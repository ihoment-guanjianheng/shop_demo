package com.gjh.shopdemo.controller;

import com.gjh.shopdemo.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sku")
public class SkuController {

    @Autowired
    private SkuService skuService;
}