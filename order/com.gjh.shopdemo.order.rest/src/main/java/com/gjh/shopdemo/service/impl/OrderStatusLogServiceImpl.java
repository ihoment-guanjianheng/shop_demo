package com.gjh.shopdemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjh.shopdemo.mapper.OrderStatusLogMapper;
import com.gjh.shopdemo.pojo.model.OrderStatusLog;
import com.gjh.shopdemo.service.OrderStatusLogService;
import org.springframework.stereotype.Service;

@Service
public class OrderStatusLogServiceImpl extends ServiceImpl<OrderStatusLogMapper, OrderStatusLog> implements OrderStatusLogService {
}