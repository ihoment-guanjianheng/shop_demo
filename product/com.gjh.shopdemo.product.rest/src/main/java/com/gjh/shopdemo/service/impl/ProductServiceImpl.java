package com.gjh.shopdemo.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjh.shopdemo.constant.RedisConstant;
import com.gjh.shopdemo.context.AuthContext;
import com.gjh.shopdemo.mapper.ProductMapper;
import com.gjh.shopdemo.oss.OssService;
import com.gjh.shopdemo.pojo.dto.ProductAddDTO;
import com.gjh.shopdemo.pojo.dto.ProductPageQueryDTO;
import com.gjh.shopdemo.pojo.dto.ProductUpdateDTO;
import com.gjh.shopdemo.pojo.exception.BaseException;
import com.gjh.shopdemo.pojo.model.Product;
import com.gjh.shopdemo.pojo.model.Sku;
import com.gjh.shopdemo.pojo.vo.ProductDetailVO;
import com.gjh.shopdemo.pojo.vo.ProductExportVO;
import com.gjh.shopdemo.pojo.vo.UserInfoVO;
import com.gjh.shopdemo.service.ProductService;
import com.gjh.shopdemo.service.SkuService;
import com.gjh.shopdemo.util.RedisCacheUtils;
import com.gjh.shopdemo.util.UUIDUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private SkuService skuService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${product.cache.TTL}")
    private Long cacheTTL;

    @Autowired
    private RedisCacheUtils redisCacheUtils;

    @Autowired
    private OssService ossService;

    private static final long EXPORT_PRESIGN_EXPIRES_SECONDS = 3600L;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addProduct(ProductAddDTO dto) {
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        UserInfoVO currentUser = AuthContext.getCurrentUser();
        if (currentUser != null && currentUser.getId() != null) {
            product.setUserId(currentUser.getId());
        }
        baseMapper.insert(product);
    }

    @Override
    public IPage<Product> pageQuery(ProductPageQueryDTO dto) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(dto.getUserId() != null, Product::getUserId, dto.getUserId())
                .like(StringUtils.isNotBlank(dto.getProductName()), Product::getProductName, dto.getProductName())
                .eq(dto.getStatus() != null, Product::getStatus, dto.getStatus())
                .orderByDesc(Product::getCreateTime);
        return page(new Page<>(dto.getCurrent(), dto.getSize()), wrapper);
    }

    @Override
    public ProductDetailVO detail(Long id) {
        String key = RedisConstant.PRODUCT_DETAIL_KEY + id;
        // 缓存有效期随机加一些时间，防止缓存雪崩
        long ttl = cacheTTL + RandomUtils.nextLong(0, 60);
        ProductDetailVO vo = null;
        boolean redisAvailable = true;

        try {
            vo = (ProductDetailVO) redisTemplate.opsForValue().get(key);
            if (vo != null) {
                // 缓存存在，更新缓存有效期，防止缓存击穿
                redisTemplate.expire(key, ttl, TimeUnit.SECONDS);
                return vo;
            }
        } catch (Exception e) {
            log.warn("Redis 读取失败，降级到数据库查询, productId={}", id, e);
            redisAvailable = false;
        }

        Product product = baseMapper.selectById(id);
        if (product == null) {
            if (redisAvailable) {
                try {
                    // 商品不存在，设置缓存为空，防止内存穿透
                    redisTemplate.opsForValue().set(key, null, ttl, TimeUnit.SECONDS);
                } catch (Exception e) {
                    log.warn("Redis 写入空值失败, productId={}", id, e);
                }
            }
            throw new BaseException("商品不存在");
        }
        vo = new ProductDetailVO();
        BeanUtils.copyProperties(product, vo);

        LambdaQueryWrapper<Sku> skuWrapper = new LambdaQueryWrapper<>();
        skuWrapper.eq(Sku::getProductId, id).orderByAsc(Sku::getId);
        List<Sku> skuList = skuService.list(skuWrapper);
        vo.setSkuList(skuList);

        if (redisAvailable) {
            try {
                redisTemplate.opsForValue().set(key, vo, ttl, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.warn("Redis 写入缓存失败, productId={}", id, e);
            }
        }
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateStatus(Long id, Integer status) {
        Product product = baseMapper.selectById(id);
        if (product == null) {
            throw new BaseException("商品不存在");
        }
        Product update = new Product();
        update.setId(id);
        update.setStatus(status);
        baseMapper.updateById(update);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateProduct(ProductUpdateDTO dto) {
        Product product = baseMapper.selectById(dto.getId());
        if (product == null) {
            throw new BaseException("商品不存在");
        }
        Product update = new Product();
        update.setId(dto.getId());
        if (dto.getProductCode() != null) {
            update.setProductCode(dto.getProductCode());
        }
        if (dto.getProductName() != null) {
            update.setProductName(dto.getProductName());
        }
        if (dto.getDescription() != null) {
            update.setDescription(dto.getDescription());
        }
        if (dto.getMainImage() != null) {
            update.setMainImage(dto.getMainImage());
        }
        if (dto.getStatus() != null) {
            update.setStatus(dto.getStatus());
        }
        baseMapper.updateById(update);
        redisCacheUtils.delayDoubleDelete(RedisConstant.PRODUCT_DETAIL_KEY + product.getId());
    }

    @Override
    public String export() {
        List<Product> products = list();
        List<ProductExportVO> rows = products.stream().map(p -> {
            ProductExportVO vo = new ProductExportVO();
            BeanUtils.copyProperties(p, vo);
            vo.setStatus(Integer.valueOf(1).equals(p.getStatus()) ? "上架" : "下架");
            vo.setCreateTime(p.getCreateTime() != null ? p.getCreateTime().toString() : "");
            return vo;
        }).collect(Collectors.toList());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        EasyExcel.write(baos, ProductExportVO.class).sheet("商品列表").doWrite(rows);

        String date = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String objectKey = "products/export/商品列表" + date + ".xlsx";
        ossService.upload(objectKey, new ByteArrayInputStream(baos.toByteArray()));
        return ossService.generatePresignedGetUrl(objectKey, EXPORT_PRESIGN_EXPIRES_SECONDS);
    }
}