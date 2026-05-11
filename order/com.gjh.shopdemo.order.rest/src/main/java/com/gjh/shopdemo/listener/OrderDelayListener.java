package com.gjh.shopdemo.listener;

import com.gjh.shopdemo.constant.RedisConstant;
import com.gjh.shopdemo.pojo.model.ShopOrder;
import com.gjh.shopdemo.pojo.mq.Message;
import com.gjh.shopdemo.service.ShopOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RocketMQMessageListener(topic = "order_create_delay",
        consumerGroup = "order_create_delay_consumer",
        consumeThreadNumber = 8
)
public class OrderDelayListener implements RocketMQListener<Message<ShopOrder>> {

    @Autowired
    private ShopOrderService shopOrderService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void onMessage(Message<ShopOrder> message) {
        MDC.put("traceId", message.getMetadata().get("traceId"));
        String messageId = message.getMessageId();
        String key = RedisConstant.MQ_CONSUMED_KEY_PREFIX + messageId;
        try {
            // 检查redis，防止消息重复消费
            Boolean ifAbsent = redisTemplate.opsForValue().setIfAbsent(key, "1", 24, TimeUnit.HOURS);
            if (Boolean.FALSE.equals(ifAbsent)) {
                log.error("重复消费消息，跳过处理，messageId: {}", messageId);
                return;
            }
            ShopOrder order = message.getPayload();
            shopOrderService.cancelOrder(order.getId(), "超时未支付");
            log.info("订单超时未支付，自动取消，orderNo: {}", order.getOrderNo());
        } catch (Exception e) {
            // 处理失败，重新入队
            log.error("处理延迟取消订单消息失败，重新入队，messageId: {}", message.getMessageId(), e);
            redisTemplate.delete(key);
        } finally {
            MDC.clear();
        }
    }
}
