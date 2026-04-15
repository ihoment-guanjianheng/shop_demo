package com.gjh.shopdemo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gjh.shopdemo.pojo.model.ShopOrder;

public interface ShopOrderService extends IService<ShopOrder> {
    String testNacos();
}