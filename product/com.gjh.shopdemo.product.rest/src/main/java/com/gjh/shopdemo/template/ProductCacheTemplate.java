package com.gjh.shopdemo.template;

import com.gjh.shopdemo.mapper.ProductMapper;
import com.gjh.shopdemo.pojo.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ProductCacheTemplate extends CacheTemplate<Product> {

    private static final String KEY_PREFIX = "cache:product:";

    @Autowired
    private ProductMapper productMapper;

    @Resource
    private RedisTemplate<String, Product> redisTemplate;

    @Override
    public Product getDataFromCache(Long id) {
        return redisTemplate.opsForValue().get(KEY_PREFIX + id);
    }

    @Override
    public Product getDataFromDb(Long id) {
        return productMapper.selectById(id);
    }



    @Override
    public void saveDataToCache(Long id, Product data) {
        redisTemplate.opsForValue().set(KEY_PREFIX + id, data);
    }

    @Override
    public void removeDataFromCache(Long id) {
        redisTemplate.delete(KEY_PREFIX + id);
    }
}
