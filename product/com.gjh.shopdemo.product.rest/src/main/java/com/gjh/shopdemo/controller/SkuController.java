package com.gjh.shopdemo.controller;

import com.gjh.shopdemo.pojo.dto.SkuAddDTO;
import com.gjh.shopdemo.pojo.result.ShopResult;
import com.gjh.shopdemo.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/sku")
public class SkuController {

    @Autowired
    private SkuService skuService;

    @PostMapping("/add")
    public ShopResult<Void> add(@Valid @RequestBody SkuAddDTO dto) {
        skuService.addSku(dto);
        return ShopResult.success();
    }
}