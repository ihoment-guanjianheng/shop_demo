package com.gjh.shopdemo.listener.handler;

import com.gjh.shopdemo.pojo.exception.BaseException;
import com.gjh.shopdemo.pojo.model.ShopOrder;
import com.gjh.shopdemo.service.ShopOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderLoadHandler extends AbstractOrderDelayHandler {

    @Autowired
    private ShopOrderService shopOrderService;

    @Override
    public void handle(OrderDelayContext ctx) {
        ShopOrder order = shopOrderService.getById(ctx.getOrderId());
        if (order == null) {
            throw new BaseException("订单不存在，orderId=" + ctx.getOrderId());
        }
        ctx.setOrder(order);
        passToNext(ctx);
    }
}