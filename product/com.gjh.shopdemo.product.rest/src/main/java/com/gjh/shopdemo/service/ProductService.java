package com.gjh.shopdemo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gjh.shopdemo.pojo.model.Product;
import com.gjh.shopdemo.pojo.dto.ProductAddDTO;

public interface ProductService extends IService<Product> {

    void addProduct(ProductAddDTO dto);
}