package com.gjh.shopdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjh.shopdemo.constant.RedisConstant;
import com.gjh.shopdemo.mapper.SkuMapper;
import com.gjh.shopdemo.pojo.dto.SkuAddDTO;
import com.gjh.shopdemo.pojo.dto.SkuPageQueryDTO;
import com.gjh.shopdemo.pojo.dto.SkuUpdateDTO;
import com.gjh.shopdemo.pojo.exception.BaseException;
import com.gjh.shopdemo.pojo.model.Product;
import com.gjh.shopdemo.pojo.model.Sku;
import com.gjh.shopdemo.product.client.remote.pojo.vo.SkuStockVO;
import com.gjh.shopdemo.service.ProductService;
import com.gjh.shopdemo.service.SkuService;
import com.gjh.shopdemo.util.RedisCacheUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class SkuServiceImpl extends ServiceImpl<SkuMapper, Sku> implements SkuService {

    @Autowired
    private ProductService productService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisCacheUtils redisCacheUtils;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addSku(SkuAddDTO dto) {
        Product product = productService.getById(dto.getProductId());
        if (product == null) {
            throw new BaseException("商品不存在");
        }
        Sku sku = new Sku();
        BeanUtils.copyProperties(dto, sku);
        baseMapper.insert(sku);
    }

    @Override
    public IPage<Sku> pageQuery(SkuPageQueryDTO dto) {
        LambdaQueryWrapper<Sku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dto.getProductId() != null, Sku::getProductId, dto.getProductId())
                .orderByAsc(Sku::getId);
        return page(new Page<>(dto.getCurrent(), dto.getSize()), wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deltaStock(Long id, Integer delta) {
        Sku sku = baseMapper.selectById(id);
        if (sku == null) {
            throw new BaseException("SKU不存在");
        }
        if (delta == null || delta == 0) {
            throw new BaseException("库存调整量不合法");
        }

        String stockKey = "stock:sku:" + id;

        // DB 原子增量更新
        baseMapper.update(null,
                new LambdaUpdateWrapper<Sku>()
                        .eq(Sku::getId, id)
                        .setSql("stock = stock + " + delta));

        // Redis 原子增量更新（扣减时用 Lua 检查库存不足）
        if (delta < 0) {
            String lua =
                    "local key = KEYS[1] " +
                    "local deduct = tonumber(ARGV[1]) " +
                    "local current = tonumber(redis.call('get', key) or 0) " +
                    "if current < deduct then return -2 end " +
                    "return redis.call('decrby', key, deduct)";
            Long result = stringRedisTemplate.execute(
                    new DefaultRedisScript<>(lua, Long.class),
                    Collections.singletonList(stockKey),
                    String.valueOf(-delta)
            );
            if (result == null || result == -2) {
                throw new BaseException("库存不足");
            }
        } else {
            stringRedisTemplate.opsForValue().increment(stockKey, delta);
        }

        redisCacheUtils.delayDoubleDelete(RedisConstant.PRODUCT_DETAIL_KEY + sku.getProductId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateSku(SkuUpdateDTO dto) {
        Sku sku = baseMapper.selectById(dto.getId());
        if (sku == null) {
            throw new BaseException("SKU不存在");
        }
        Sku update = new Sku();
        update.setId(dto.getId());
        if (dto.getSkuCode() != null) {
            update.setSkuCode(dto.getSkuCode());
        }
        if (dto.getSkuName() != null) {
            update.setSkuName(dto.getSkuName());
        }
        if (dto.getSkuSpecs() != null) {
            update.setSkuSpecs(dto.getSkuSpecs());
        }
        if (dto.getPrice() != null) {
            update.setPrice(dto.getPrice());
        }
        if (dto.getStock() != null) {
            update.setStock(dto.getStock());
        }
        if (dto.getLockStock() != null) {
            update.setLockStock(dto.getLockStock());
        }
        if (dto.getImage() != null) {
            update.setImage(dto.getImage());
        }
        if (dto.getStatus() != null) {
            update.setStatus(dto.getStatus());
        }
        baseMapper.updateById(update);
        redisCacheUtils.delayDoubleDelete(RedisConstant.PRODUCT_DETAIL_KEY + sku.getProductId());
    }

    @Override
    public SkuStockVO getStockById(Long id) {
        Sku sku = baseMapper.selectById(id);
        if (sku == null) {
            throw new BaseException("SKU不存在");
        }
        SkuStockVO skuStockVO = new SkuStockVO();
        skuStockVO.setStock(sku.getStock());
        skuStockVO.setId(sku.getId());
        return skuStockVO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deductDbStock(Long id, Integer quantity) {
        Sku sku = baseMapper.selectById(id);
        if (sku == null) {
            throw new BaseException("SKU不存在");
        }
        if (quantity == null || quantity <= 0) {
            throw new BaseException("扣减数量不合法");
        }
        int affected = baseMapper.update(null,
                new LambdaUpdateWrapper<Sku>()
                        .eq(Sku::getId, id)
                        .ge(Sku::getStock, quantity)
                        .setSql("stock = stock - " + quantity));
        if (affected == 0) {
            throw new BaseException("库存不足");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteSku(Long id) {
        Sku sku = baseMapper.selectById(id);
        if (sku == null) {
            throw new BaseException("SKU不存在");
        }
        removeById(id);
        redisCacheUtils.delayDoubleDelete(RedisConstant.PRODUCT_DETAIL_KEY + sku.getProductId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addDbStock(Long id, Integer quantity) {
        Sku sku = baseMapper.selectById(id);
        if (sku == null) {
            throw new BaseException("SKU不存在");
        }
        if (quantity == null || quantity <= 0) {
            throw new BaseException("回滚数量不合法");
        }
        baseMapper.update(null,
                new LambdaUpdateWrapper<Sku>()
                        .eq(Sku::getId, id)
                        .setSql("stock = stock + " + quantity));
    }
}