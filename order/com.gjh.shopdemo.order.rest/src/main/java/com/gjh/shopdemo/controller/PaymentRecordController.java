package com.gjh.shopdemo.controller;

import com.gjh.shopdemo.service.PaymentRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/paymentRecord")
public class PaymentRecordController {

    @Autowired
    private PaymentRecordService paymentRecordService;
}