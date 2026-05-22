package com.gjh.shopdemo.strategy;

import com.gjh.shopdemo.pojo.exception.BaseException;
import com.gjh.shopdemo.pojo.model.OrderItem;
import com.gjh.shopdemo.pojo.model.ShopOrder;
import com.gjh.shopdemo.pojo.mq.dto.StockUpdateMqDTO;
import com.gjh.shopdemo.pojo.result.ShopResult;
import com.gjh.shopdemo.product.client.remote.client.SkuFeignRemoteClient;
import com.gjh.shopdemo.product.client.remote.pojo.vo.SkuStockVO;
import com.gjh.shopdemo.util.MqMessageUtils;
import com.gjh.shopdemo.util.RedisLockUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 预占扣减策略：创建订单时预占库存，支付成功后异步确认扣减
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "stock.strategy", havingValue = "preoccupy", matchIfMissing = true)
public class PreoccupyDeductStrategy implements StockStrategy {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SkuFeignRemoteClient skuFeignClient;

    @Autowired
    private RedisLockUtils redisLockUtils;

    @Autowired
    private MqMessageUtils<ShopOrder> mqMessageUtils;

    private static final String STOCK_KEY_PREFIX = "stock:sku:";
    private static final String LOCK_KEY_PREFIX = "lock:stock:sku:";

    /**
     * 预占库存 Lua 脚本
     * KEYS[1]: stockKey  KEYS[2]: lockKey
     * ARGV[1]: quantity
     * 返回 1: 成功  -1: 库存未初始化  -2: 库存不足
     */
    private static final String PREOCCUPY_LUA =
            "local stockKey = KEYS[1] " +
                    "local lockKey = KEYS[2] " +
                    "local qty = tonumber(ARGV[1]) " +
                    "local stock = redis.call('get', stockKey) " +
                    "if stock == false then return -1 end " +
                    "local current = tonumber(stock) " +
                    "if current < qty then return -2 end " +
                    "redis.call('decrby', stockKey, qty) " +
                    "redis.call('incrby', lockKey, qty) " +
                    "return 1";

    /**
     * 确认扣减 Lua 脚本
     * KEYS[1]: lockKey
     * ARGV[1]: quantity
     * 返回 1: 成功  -2: 预占库存不足
     */
    private static final String CONFIRM_LUA =
            "local lockKey = KEYS[1] " +
                    "local qty = tonumber(ARGV[1]) " +
                    "local locked = tonumber(redis.call('get', lockKey) or 0) " +
                    "if locked < qty then return -2 end " +
                    "redis.call('decrby', lockKey, qty) " +
                    "return 1";

    /**
     * 释放库存 Lua 脚本
     * KEYS[1]: stockKey  KEYS[2]: lockKey
     * ARGV[1]: quantity
     * 返回 1: 成功
     */
    private static final String RELEASE_LUA =
            "local stockKey = KEYS[1] " +
                    "local lockKey = KEYS[2] " +
                    "local qty = tonumber(ARGV[1]) " +
                    "redis.call('incrby', stockKey, qty) " +
                    "redis.call('decrby', lockKey, qty) " +
                    "return 1";

    @Override
    public boolean preoccupy(Long skuId, Integer quantity) {
        String stockKey = STOCK_KEY_PREFIX + skuId;
        String lockKey = LOCK_KEY_PREFIX + skuId;
        Long result = stringRedisTemplate.execute(
                new DefaultRedisScript<>(PREOCCUPY_LUA, Long.class),
                Arrays.asList(stockKey, lockKey),
                String.valueOf(quantity)
        );

        if (result != null && result == -1) {
            String initLockKey = "lock:stock:init:" + skuId;
            boolean locked = redisLockUtils.tryLock(initLockKey, 10, TimeUnit.SECONDS);
            if (locked) {
                try {
                    result = stringRedisTemplate.execute(
                            new DefaultRedisScript<>(PREOCCUPY_LUA, Long.class),
                            Arrays.asList(stockKey, lockKey),
                            String.valueOf(quantity)
                    );
                    if (result != null && result == 1) {
                        return true;
                    }
                    if (result != null && result == -2) {
                        return false;
                    }

                    ShopResult<SkuStockVO> shopResult = skuFeignClient.getStockById(skuId);
                    if (shopResult == null || shopResult.getData() == null || shopResult.getData().getStock() == null) {
                        return false;
                    }
                    stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(shopResult.getData().getStock()));

                    result = stringRedisTemplate.execute(
                            new DefaultRedisScript<>(PREOCCUPY_LUA, Long.class),
                            Arrays.asList(stockKey, lockKey),
                            String.valueOf(quantity)
                    );
                    return result != null && result == 1;
                } catch (Exception e) {
                    throw new BaseException("库存扣减失败，请稍后重试");
                } finally {
                    redisLockUtils.unlock(initLockKey);
                }
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                result = stringRedisTemplate.execute(
                        new DefaultRedisScript<>(PREOCCUPY_LUA, Long.class),
                        Arrays.asList(stockKey, lockKey),
                        String.valueOf(quantity)
                );
                return result != null && result == 1;
            }
        }

        return result != null && result == 1;
    }

    @Override
    public boolean confirm(Long skuId, Integer quantity) {
        String lockKey = LOCK_KEY_PREFIX + skuId;
        Long result = stringRedisTemplate.execute(
                new DefaultRedisScript<>(CONFIRM_LUA, Long.class),
                Arrays.asList(lockKey),
                String.valueOf(quantity)
        );
        return result != null && result == 1;
    }

    @Override
    public void afterPayCommit(List<OrderItem> items) {
        for (OrderItem item : items) {
            StockUpdateMqDTO dto = new StockUpdateMqDTO(item.getSkuId(), item.getQuantity());
            boolean sent = false;
            int retries = 3;
            for (int i = 0; i < retries; i++) {
                try {
                    boolean ok = mqMessageUtils.sendOrderlyMessage(
                            "order_create", "stock.confirm",
                            item.getSkuId(), dto,
                            String.valueOf(item.getSkuId())
                    );
                    if (ok) {
                        sent = true;
                        break;
                    }
                    log.warn("库存确认消息发送未返回 OK，第 {} 次重试，skuId={}", i + 1, item.getSkuId());
                } catch (Exception e) {
                    log.warn("库存确认消息发送异常，第 {} 次重试，skuId={}", i + 1, item.getSkuId(), e);
                }
                try {
                    Thread.sleep(100L * (i + 1));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            if (!sent) {
                log.error("库存确认消息发送最终失败，skuId={}, quantity={}，将导致 DB 库存未扣减",
                        item.getSkuId(), item.getQuantity());
            }
        }
    }

    @Override
    public boolean release(Long skuId, Integer quantity) {
        String stockKey = STOCK_KEY_PREFIX + skuId;
        String lockKey = LOCK_KEY_PREFIX + skuId;
        Long result = stringRedisTemplate.execute(
                new DefaultRedisScript<>(RELEASE_LUA, Long.class),
                Arrays.asList(stockKey, lockKey),
                String.valueOf(quantity)
        );
        return result != null && result == 1;
    }

    @Override
    public boolean sendDelayOrderCreatedMessage(ShopOrder shopOrder, Long delayTime) {
        return mqMessageUtils.sendDelayMessage("order_create_delay", "order.create.delay", shopOrder.getId(), shopOrder, delayTime);
    }
}