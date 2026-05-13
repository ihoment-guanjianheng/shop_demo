package com.gjh.shopdemo.listener;

import com.gjh.shopdemo.listener.handler.*;
import com.gjh.shopdemo.pojo.model.ShopOrder;
import com.gjh.shopdemo.pojo.mq.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
@RocketMQMessageListener(topic = "order_create_delay",
        consumerGroup = "order_create_delay_consumer",
        consumeThreadNumber = 8
)
public class OrderDelayListener implements RocketMQListener<Message<ShopOrder>> {

    @Autowired
    private IdempotencyCheckHandler idempotencyCheckHandler;
    @Autowired
    private OrderLoadHandler orderLoadHandler;
    @Autowired
    private StatusGuardHandler statusGuardHandler;
    @Autowired
    private CancelOrderHandler cancelOrderHandler;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private AbstractOrderDelayHandler chain;

    @PostConstruct
    private void buildChain() {
        idempotencyCheckHandler
                .setNext(orderLoadHandler)
                .setNext(statusGuardHandler)
                .setNext(cancelOrderHandler);
        chain = idempotencyCheckHandler;
    }

    @Override
    public void onMessage(Message<ShopOrder> message) {
        MDC.put("traceId", message.getMetadata().get("traceId"));
        OrderDelayContext ctx = new OrderDelayContext(message);
        try {
            chain.handle(ctx);
        } catch (Exception e) {
            log.error("处理延迟取消订单消息失败，重新入队，messageId={}", ctx.getMessageId(), e);
            redisTemplate.delete(ctx.getRedisKey());
        } finally {
            MDC.clear();
        }
    }
}