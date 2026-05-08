package com.gjh.shopdemo.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisCacheUtils {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 延迟双删：先删除缓存，延迟指定时间后再次删除，防止并发脏写
     *
     * @param cacheKey      缓存key
     * @param delayMillis   延迟时间（毫秒）
     */
    public void delayDoubleDelete(String cacheKey, long delayMillis) {
        redisTemplate.delete(cacheKey);
        new Thread(() -> {
            try {
                Thread.sleep(delayMillis);
                redisTemplate.delete(cacheKey);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * 延迟双删，默认延迟 200ms
     *
     * @param cacheKey 缓存key
     */
    public void delayDoubleDelete(String cacheKey) {
        delayDoubleDelete(cacheKey, 200);
    }
}