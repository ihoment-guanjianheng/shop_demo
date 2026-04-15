package com.gjh.shopdemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjh.shopdemo.mapper.PaymentRecordMapper;
import com.gjh.shopdemo.pojo.model.PaymentRecord;
import com.gjh.shopdemo.service.PaymentRecordService;
import org.springframework.stereotype.Service;

@Service
public class PaymentRecordServiceImpl extends ServiceImpl<PaymentRecordMapper, PaymentRecord> implements PaymentRecordService {
}