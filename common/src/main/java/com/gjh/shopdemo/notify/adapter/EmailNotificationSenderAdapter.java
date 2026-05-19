package com.gjh.shopdemo.notify.adapter;

import com.gjh.shopdemo.notify.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component("email")
@ConditionalOnClass(name = "org.springframework.mail.javamail.JavaMailSender")
@Slf4j
public class EmailNotificationSenderAdapter implements NotificationSender {

    @Resource
    private JavaMailSender mailSender;

    @Value("${notification.email.from:}")
    private String from;

    @Override
    public void send(String message, List<String> receivers) {
        if (mailSender == null) {
            log.warn("JavaMailSender 未配置，跳过邮件发送");
            return;
        }
        if (receivers == null || receivers.isEmpty()) {
            log.warn("邮件接收人为空，跳过发送");
            return;
        }
        if (from == null || from.isEmpty()) {
            log.warn("邮件发件人未配置，跳过发送");
            return;
        }
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(from);
            mail.setTo(receivers.toArray(new String[0]));
            mail.setSubject("系统通知");
            mail.setText(message);
            mailSender.send(mail);
            log.info("邮件发送成功, receivers={}", receivers);
        } catch (Exception e) {
            log.error("邮件发送失败, receivers={}", receivers, e);
            throw new RuntimeException("邮件发送失败", e);
        }
    }
}