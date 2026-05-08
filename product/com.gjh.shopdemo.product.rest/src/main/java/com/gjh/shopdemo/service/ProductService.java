package com.gjh.shopdemo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gjh.shopdemo.pojo.dto.ProductAddDTO;
import com.gjh.shopdemo.pojo.dto.ProductPageQueryDTO;
import com.gjh.shopdemo.pojo.dto.ProductUpdateDTO;
import com.gjh.shopdemo.pojo.model.Product;
import com.gjh.shopdemo.pojo.vo.ProductDetailVO;

public interface ProductService extends IService<Product> {

    void addProduct(ProductAddDTO dto);

    IPage<Product> pageQuery(ProductPageQueryDTO dto);

    ProductDetailVO detail(Long id);

    void updateStatus(Long id, Integer status);

    void updateProduct(ProductUpdateDTO dto);
}