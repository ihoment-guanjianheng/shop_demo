package com.gjh.shopdemo.controller;

import com.gjh.shopdemo.service.ShopOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shopOrder")
public class ShopOrderController {

    @Autowired
    private ShopOrderService shopOrderService;
}