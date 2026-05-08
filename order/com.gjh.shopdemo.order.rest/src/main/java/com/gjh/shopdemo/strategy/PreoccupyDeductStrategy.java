package com.gjh.shopdemo.strategy;

import com.gjh.shopdemo.pojo.result.ShopResult;
import com.gjh.shopdemo.product.client.remote.client.SkuFeignRemoteClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 预占扣减策略：创建订单时预占库存，支付成功后异步确认扣减
 */
@Component
@ConditionalOnProperty(name = "stock.strategy", havingValue = "preoccupy")
public class PreoccupyDeductStrategy implements StockStrategy {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private SkuFeignRemoteClient skuFeignClient;

    private static final String STOCK_KEY_PREFIX = "stock:sku:";
    private static final String LOCK_KEY_PREFIX = "lock:stock:sku:";

    /**
     * 预占库存 Lua 脚本
     * KEYS[1]: stockKey  KEYS[2]: lockKey
     * ARGV[1]: quantity
     * 返回 1: 成功  -2: 库存不足
     */
    private static final String PREOCCUPY_LUA =
            "local stockKey = KEYS[1] " +
            "local lockKey = KEYS[2] " +
            "local qty = tonumber(ARGV[1]) " +
            "local stock = tonumber(redis.call('get', stockKey) or 0) " +
            "if stock < qty then return -2 end " +
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
        if (result == null || result != 1) {
            return false;
        }
        // 异步扣减 DB 库存
        ShopResult<Void> dbResult = skuFeignClient.deductDbStock(skuId, quantity);
        return dbResult != null && dbResult.getCode() == 200;
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
}