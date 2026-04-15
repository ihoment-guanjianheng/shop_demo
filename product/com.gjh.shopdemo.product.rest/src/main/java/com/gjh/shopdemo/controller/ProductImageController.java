package com.gjh.shopdemo.controller;

import com.gjh.shopdemo.service.ProductImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/productImage")
public class ProductImageController {

    @Autowired
    private ProductImageService productImageService;
}