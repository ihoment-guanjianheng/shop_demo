package com.gjh.shopdemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjh.shopdemo.mapper.SkuMapper;
import com.gjh.shopdemo.pojo.exception.BaseException;
import com.gjh.shopdemo.pojo.model.Product;
import com.gjh.shopdemo.pojo.model.Sku;
import com.gjh.shopdemo.pojo.request.SkuAddRequest;
import com.gjh.shopdemo.service.ProductService;
import com.gjh.shopdemo.service.SkuService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SkuServiceImpl extends ServiceImpl<SkuMapper, Sku> implements SkuService {

    @Autowired
    private ProductService productService;

    @Override
    public void addSku(SkuAddRequest request) {
        Product product = productService.getById(request.getProductId());
        if (product == null) {
            throw new BaseException("商品不存在");
        }
        Sku sku = new Sku();
        BeanUtils.copyProperties(request, sku);
        baseMapper.insert(sku);
    }
}