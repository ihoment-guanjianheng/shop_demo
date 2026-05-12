package com.gjh.shopdemo.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gjh.shopdemo.pojo.model.ShopOrder;
import com.gjh.shopdemo.service.ShopOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class ExpiredOrderCancelTask {

    @Autowired
    private ShopOrderService shopOrderService;

    @Scheduled(fixedDelay = 60_000)
    public void cancelExpiredOrders() {
        List<ShopOrder> expired = shopOrderService.list(
                new LambdaQueryWrapper<ShopOrder>()
                        .eq(ShopOrder::getStatus, 0)
                        .le(ShopOrder::getExpireTime, LocalDateTime.now())
                        .last("LIMIT 100")
        );
        if (expired.isEmpty()) {
            return;
        }
        log.info("扫描到 {} 笔超时未支付订单，开始批量取消", expired.size());
        for (ShopOrder order : expired) {
            try {
                shopOrderService.cancelOrder(order.getId(), "超时未支付自动取消");
            } catch (Exception e) {
                log.error("定时取消订单失败，orderId={}", order.getId(), e);
            }
        }
    }
}