package com.gjh.shopdemo.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisLockUtils {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 尝试获取分布式锁
     *
     * @param lockKey    锁的key
     * @param expireTime 锁的过期时间
     * @param timeUnit   时间单位
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, long expireTime, TimeUnit timeUnit) {
        Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", expireTime, timeUnit);
        return Boolean.TRUE.equals(locked);
    }

    /**
     * 释放分布式锁
     *
     * @param lockKey 锁的key
     */
    public void unlock(String lockKey) {
        stringRedisTemplate.delete(lockKey);
    }

    /**
     * 尝试获取锁并执行操作，执行完毕后自动释放锁
     *
     * @param lockKey    锁的key
     * @param expireTime 锁的过期时间
     * @param timeUnit   时间单位
     * @param action     获取锁后执行的操作
     * @return 是否成功获取锁并执行操作
     */
    public boolean tryLockThenRun(String lockKey, long expireTime, TimeUnit timeUnit, Runnable action) {
        boolean locked = tryLock(lockKey, expireTime, timeUnit);
        if (!locked) {
            return false;
        }
        try {
            action.run();
            return true;
        } finally {
            unlock(lockKey);
        }
    }
}