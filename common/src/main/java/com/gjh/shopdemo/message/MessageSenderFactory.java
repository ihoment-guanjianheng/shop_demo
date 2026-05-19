package com.gjh.shopdemo.message;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MessageSenderFactory {

    @Autowired
    private Map<String, MessageSender> messageSenders;

    public MessageSender getMessageSender(String type) {
        return messageSenders.get(type);
    }
}
