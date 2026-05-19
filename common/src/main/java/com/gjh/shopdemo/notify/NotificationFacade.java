package com.gjh.shopdemo.notify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 通知统一门面，业务代码只调用此类发送通知
 */
@Component
@Slf4j
public class NotificationFacade {

    @Autowired
    private NotificationProperties notificationProperties;

    @Autowired
    private NotificationSenderFactory notificationSenderFactory;

    /**
     * 发送通知：根据事件类型从 Nacos 配置中读取对应渠道，依次调用适配器
     */
    public void send(NotificationMessage message) {
        if (message == null || message.getEventType() == null) {
            log.warn("通知消息为空或事件类型为空");
            return;
        }

        List<String> channelTypes = notificationProperties.getChannelsForEvent(message.getEventType());
        if (channelTypes.isEmpty()) {
            log.warn("事件类型 {} 未配置通知渠道", message.getEventType());
            return;
        }

        for (String type : channelTypes) {
            NotificationSender sender = notificationSenderFactory.getMessageSender(type);
            if (sender == null) {
                log.warn("未找到渠道 {} 对应的 NotificationSender", type);
                continue;
            }
            try {
                sender.send(message.getContent(), message.getReceivers());
                log.info("通知发送成功, eventType={}, channel={}", message.getEventType(), type);
            } catch (Exception e) {
                log.error("通知发送失败, eventType={}, channel={}", message.getEventType(), type, e);
            }
        }
    }
}