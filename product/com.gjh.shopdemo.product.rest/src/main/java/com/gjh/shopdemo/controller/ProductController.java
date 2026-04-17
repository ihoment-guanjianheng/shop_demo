package com.gjh.shopdemo.controller;

import com.gjh.shopdemo.pojo.dto.ProductAddDTO;
import com.gjh.shopdemo.pojo.result.ShopResult;
import com.gjh.shopdemo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping("/add")
    public ShopResult<Void> add(@Valid @RequestBody ProductAddDTO dto) {
        productService.addProduct(dto);
        return ShopResult.success();
    }
}