package com.gjh.shopdemo.controller;

import com.gjh.shopdemo.pojo.exception.BaseException;
import com.gjh.shopdemo.pojo.result.ShopResult;
import com.gjh.shopdemo.service.ShopOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.lang.Thread.sleep;

@RestController
@RequestMapping("/shopOrder")
public class ShopOrderController {

    @Autowired
    private ShopOrderService shopOrderService;

    @GetMapping("/testNacos")
    public ShopResult<String> testNacos() {
        return ShopResult.success(shopOrderService.testNacos());
    }
}