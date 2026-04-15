package com.gjh.shopdemo.controller;

import com.gjh.shopdemo.service.OrderStatusLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orderStatusLog")
public class OrderStatusLogController {

    @Autowired
    private OrderStatusLogService orderStatusLogService;
}