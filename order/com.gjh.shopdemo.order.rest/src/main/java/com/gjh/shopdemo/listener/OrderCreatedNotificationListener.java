package com.gjh.shopdemo.listener;

import com.gjh.shopdemo.message.NotificationFacade;
import com.gjh.shopdemo.message.NotificationMessage;
import com.gjh.shopdemo.pojo.mq.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RocketMQMessageListener(
        topic = "order_created_notification",
        consumerGroup = "order_created_notification_consumer"
)
public class OrderCreatedNotificationListener implements RocketMQListener<Message<NotificationMessage>> {

    @Autowired
    private NotificationFacade notificationFacade;

    @Override
    public void onMessage(Message<NotificationMessage> message) {
        log.info("收到订单创建通知, messageId={}", message.getMessageId());
        if (message.getMetadata() != null && message.getMetadata().get("traceId") != null) {
            MDC.put("traceId", message.getMetadata().get("traceId"));
        }
        try {
            NotificationMessage notification = message.getPayload();
            if (notification == null) {
                log.warn("订单创建通知消息体为空, messageId={}", message.getMessageId());
                return;
            }
            log.info("处理订单创建通知, messageId={}, eventType={}", message.getMessageId(), notification.getEventType());
            notificationFacade.send(notification);
        } finally {
            MDC.clear();
        }
    }
}