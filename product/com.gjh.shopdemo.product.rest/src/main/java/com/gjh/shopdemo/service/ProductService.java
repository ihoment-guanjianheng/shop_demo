package com.gjh.shopdemo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gjh.shopdemo.pojo.model.Product;
import com.gjh.shopdemo.pojo.request.ProductAddRequest;

public interface ProductService extends IService<Product> {

    void addProduct(ProductAddRequest request);
}