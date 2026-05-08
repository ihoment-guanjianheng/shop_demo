package com.gjh.shopdemo.template;

import com.gjh.shopdemo.mapper.SkuMapper;
import com.gjh.shopdemo.pojo.model.Sku;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class SkuCacheTemplate extends CacheTemplate<Sku> {

    private static final String KEY_PREFIX = "cache:sku:";

    @Autowired
    private RedisTemplate<String, Sku> redisTemplate;

    @Autowired
    private SkuMapper skuMapper;

    @Override
    public Sku getDataFromCache(Long id) {
        return redisTemplate.opsForValue().get(KEY_PREFIX + id);
    }

    @Override
    public Sku getDataFromDb(Long id) {
        return skuMapper.selectById(id);
    }

    @Override
    public void saveDataToCache(Long id, Sku data) {
        redisTemplate.opsForValue().set(KEY_PREFIX + id, data);
    }

    @Override
    public void removeDataFromCache(Long id) {
        redisTemplate.delete(KEY_PREFIX + id);
    }
}
