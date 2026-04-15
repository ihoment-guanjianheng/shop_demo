package com.gjh.shopdemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjh.shopdemo.mapper.ProductImageMapper;
import com.gjh.shopdemo.pojo.model.ProductImage;
import com.gjh.shopdemo.service.ProductImageService;
import org.springframework.stereotype.Service;

@Service
public class ProductImageServiceImpl extends ServiceImpl<ProductImageMapper, ProductImage> implements ProductImageService {
}