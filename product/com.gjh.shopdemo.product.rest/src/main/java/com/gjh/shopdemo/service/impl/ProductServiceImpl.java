package com.gjh.shopdemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjh.shopdemo.mapper.ProductMapper;
import com.gjh.shopdemo.pojo.model.Product;
import com.gjh.shopdemo.pojo.dto.ProductAddDTO;
import com.gjh.shopdemo.service.ProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addProduct(ProductAddDTO dto) {
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        baseMapper.insert(product);
    }
}