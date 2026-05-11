package com.gjh.shopdemo.util;

import com.gjh.shopdemo.context.AuthContext;
import com.gjh.shopdemo.pojo.mq.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class MqMessageUtils<T> {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public <T> boolean sendMessage(String topic, String messageType, T payload) {
        return sendMessage(topic, messageType, null, payload);
    }

    public <T> boolean sendMessage(String topic, String messageType,
                                Long aggregateId, T payload) {
        Message<T> message = Message.<T>builder()
                .messageId(UUID.randomUUID().toString())
                .messageType(messageType)
                .aggregateId(aggregateId)
                .timestamp(System.currentTimeMillis())
                .version(1)
                .payload(payload)
                .metadata(buildMetadata())  // 注入 traceId
                .build();

        SendResult sendResult = rocketMQTemplate.syncSend(
                topic,
                MessageBuilder
                        .withPayload(message)
                        .setHeader("KEYS", message.getMessageId())
                        .build()
        );
        return sendResult.getSendStatus() == SendStatus.SEND_OK;
    }

    public <T> boolean sendDelayMessage(String topic, String messageType, T payload, Long delayTime) {
        return sendDelayMessage(topic, messageType, null, payload, delayTime);
    }

    public <T> boolean sendDelayMessage(String topic, String messageType, Long aggregateId, T payload, Long delayTime) {
        Message<T> message = Message.<T>builder()
                .messageId(UUID.randomUUID().toString())
                .messageType(messageType)
                .aggregateId(aggregateId)
                .timestamp(System.currentTimeMillis())
                .version(1)
                .payload(payload)
                .metadata(buildMetadata())  // 注入 traceId
                .build();
        SendResult sendResult = rocketMQTemplate.syncSendDelayTimeMills(
                topic,
                MessageBuilder
                        .withPayload(message)
                        .setHeader("KEYS", message.getMessageId())
                        .build(),
                delayTime
        );
        return sendResult.getSendStatus() == SendStatus.SEND_OK;
    }

    private Map<String, String> buildMetadata() {
        Map<String, String> meta = new HashMap<>();
        // 注入 SkyWalking / Sleuth traceId
        meta.put("traceId", MDC.get("traceId"));
        meta.put("userId", AuthContext.getCurrentUser().getId().toString());
        return meta;
    }
}
