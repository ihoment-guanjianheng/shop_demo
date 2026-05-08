package com.gjh.shopdemo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gjh.shopdemo.pojo.dto.OrderCreateDTO;
import com.gjh.shopdemo.pojo.dto.OrderPageQueryDTO;
import com.gjh.shopdemo.pojo.model.ShopOrder;
import com.gjh.shopdemo.pojo.vo.OrderDetailVO;

public interface ShopOrderService extends IService<ShopOrder> {

    Long createOrder(OrderCreateDTO dto);

    IPage<ShopOrder> pageQuery(OrderPageQueryDTO dto);

    OrderDetailVO detail(Long id);

    void cancelOrder(Long id, String reason);

    void payOrder(Long id);

    void deliverOrder(Long id);

    void confirmReceive(Long id);
}