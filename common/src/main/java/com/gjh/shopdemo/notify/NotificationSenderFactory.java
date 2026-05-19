package com.gjh.shopdemo.notify;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationSenderFactory {

    @Autowired
    private Map<String, NotificationSender> messageSenders;

    public NotificationSender getMessageSender(String type) {
        return messageSenders.get(type);
    }
}
