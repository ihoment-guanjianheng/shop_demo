package com.gjh.shopdemo.listener.handler;

import com.gjh.shopdemo.service.ShopOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CancelOrderHandler extends AbstractOrderDelayHandler {

    @Autowired
    private ShopOrderService shopOrderService;

    @Override
    public void handle(OrderDelayContext ctx) {
        shopOrderService.cancelOrder(ctx.getOrderId(), "超时未支付");
        log.info("订单超时未支付，自动取消，orderNo={}", ctx.getOrder().getOrderNo());
    }
}