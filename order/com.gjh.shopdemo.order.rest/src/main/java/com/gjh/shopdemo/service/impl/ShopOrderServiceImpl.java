package com.gjh.shopdemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjh.shopdemo.mapper.ShopOrderMapper;
import com.gjh.shopdemo.pojo.model.ShopOrder;
import com.gjh.shopdemo.service.ShopOrderService;
import org.springframework.stereotype.Service;

@Service
public class ShopOrderServiceImpl extends ServiceImpl<ShopOrderMapper, ShopOrder> implements ShopOrderService {
}