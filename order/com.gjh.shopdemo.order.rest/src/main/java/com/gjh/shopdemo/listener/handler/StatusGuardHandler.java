package com.gjh.shopdemo.listener.handler;

import com.gjh.shopdemo.pojo.enums.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StatusGuardHandler extends AbstractOrderDelayHandler {

    @Override
    public void handle(OrderDelayContext ctx) {
        if (OrderStatus.of(ctx.getOrder().getStatus()) != OrderStatus.PENDING_PAYMENT) {
            log.info("订单已不在待支付状态，跳过取消，orderId={}, status={}",
                    ctx.getOrderId(), ctx.getOrder().getStatus());
            return;
        }
        passToNext(ctx);
    }
}