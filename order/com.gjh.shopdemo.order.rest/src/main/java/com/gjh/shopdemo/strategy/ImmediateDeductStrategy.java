package com.gjh.shopdemo.strategy;

import com.gjh.shopdemo.pojo.exception.BaseException;
import com.gjh.shopdemo.pojo.model.ShopOrder;
import com.gjh.shopdemo.pojo.result.ShopResult;
import com.gjh.shopdemo.product.client.remote.client.SkuFeignRemoteClient;
import com.gjh.shopdemo.product.client.remote.pojo.vo.SkuStockVO;
import com.gjh.shopdemo.util.MqMessageUtils;
import com.gjh.shopdemo.util.RedisLockUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 立即扣减策略：创建订单时直接扣减库存，支付无需二次确认
 */
@Component
@ConditionalOnProperty(name = "stock.strategy", havingValue = "immediate", matchIfMissing = true)
public class ImmediateDeductStrategy implements StockStrategy {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SkuFeignRemoteClient skuFeignClient;

    @Autowired
    private MqMessageUtils<ShopOrder> mqMessageUtils;

    @Autowired
    private RedisLockUtils redisLockUtils;

    private static final String STOCK_KEY_PREFIX = "stock:sku:";

    @Override
    public boolean preoccupy(Long skuId, Integer quantity) {
        boolean success = deduct(skuId, quantity);
        if (success) {
            ShopResult<Void> dbResult = skuFeignClient.deductDbStock(skuId, quantity);
            if (dbResult == null || dbResult.getCode() != 1) {
                rollback(skuId, quantity);
                return false;
            }
        }
        return success;
    }

    @Override
    public boolean confirm(Long skuId, Integer quantity) {
        // 已经扣减过了，无需操作
        return true;
    }

    @Override
    public boolean release(Long skuId, Integer quantity) {
        rollback(skuId, quantity);
        skuFeignClient.addDbStock(skuId, quantity);
        return true;
    }

    @Override
    public boolean sendDelayOrderCreatedMessage(ShopOrder shopOrder, Long delayTime) {
        return mqMessageUtils.sendDelayMessage("order_create_delay", "order.create.delay", shopOrder.getId(), shopOrder, delayTime);
    }


    /**
     * 扣减库存 Lua 脚本
     * 返回 >= 0: 扣减成功，值为扣减后的库存
     * 返回 -1: 库存未初始化
     * 返回 -2: 库存不足
     */
    private static final String DEDUCT_LUA =
            "local key = KEYS[1] " +
                    "local deduct = tonumber(ARGV[1]) " +
                    "local stock = redis.call('get', key) " +
                    "if stock == false then return -1 end " +
                    "local current = tonumber(stock) " +
                    "if current < deduct then return -2 end " +
                    "return redis.call('decrby', key, deduct)";

    /**
     * 回滚库存 Lua 脚本
     */
    private static final String ROLLBACK_LUA =
            "local key = KEYS[1] " +
                    "local quantity = tonumber(ARGV[1]) " +
                    "return redis.call('incrby', key, quantity)";

    public boolean deduct(Long skuId, Integer quantity) {
        String key = STOCK_KEY_PREFIX + skuId;
        Long result = executeDeduct(key, quantity);

        if (result != null && result == -1) {
            String lockKey = "lock:stock:init:" + skuId;
            boolean locked = redisLockUtils.tryLock(lockKey, 10, TimeUnit.SECONDS);

            if (locked) {
                try {
                    result = executeDeduct(key, quantity);
                    if (result != null && result >= 0) {
                        return true;
                    }
                    if (result != null && result == -2) {
                        return false;
                    }

                    ShopResult<SkuStockVO> shopResult = skuFeignClient.getStockById(skuId);
                    if (shopResult == null || shopResult.getData() == null || shopResult.getData().getStock() == null) {
                        return false;
                    }
                    stringRedisTemplate.opsForValue().set(key, String.valueOf(shopResult.getData().getStock()));

                    result = executeDeduct(key, quantity);
                    return result >= 0;
                } catch (Exception e) {
                    throw new BaseException("扣减库存失败,请稍后重试");
                } finally {
                    redisLockUtils.unlock(lockKey);
                }
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                result = executeDeduct(key, quantity);
                return result != null && result >= 0;
            }
        }

        return result != null && result >= 0;
    }

    public void rollback(Long skuId, Integer quantity) {
        String key = STOCK_KEY_PREFIX + skuId;
        stringRedisTemplate.execute(
                new DefaultRedisScript<>(ROLLBACK_LUA, Long.class),
                Collections.singletonList(key),
                String.valueOf(quantity)
        );
    }

    private Long executeDeduct(String key, Integer quantity) {
        return stringRedisTemplate.execute(
                new DefaultRedisScript<>(DEDUCT_LUA, Long.class),
                Collections.singletonList(key),
                String.valueOf(quantity)
        );
    }
}