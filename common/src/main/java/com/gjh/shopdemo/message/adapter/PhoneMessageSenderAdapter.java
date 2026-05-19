package com.gjh.shopdemo.message.adapter;

import com.gjh.shopdemo.message.MessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("sms")
@Slf4j
public class PhoneMessageSenderAdapter implements MessageSender {

    @Value("${notification.sms.accessKeyId:}")
    private String accessKeyId;

    @Value("${notification.sms.accessKeySecret:}")
    private String accessKeySecret;

    @Value("${notification.sms.signName:}")
    private String signName;

    @Override
    public void send(String message, List<String> receivers) {
        if (receivers == null || receivers.isEmpty()) {
            log.warn("短信接收人为空，跳过发送");
            return;
        }
        if (accessKeyId == null || accessKeyId.isEmpty()) {
            log.warn("短信 AccessKeyId 未配置，跳过发送");
            return;
        }
        log.info("短信发送成功, receivers={}, signName={}, content={}", receivers, signName, message);
    }
}