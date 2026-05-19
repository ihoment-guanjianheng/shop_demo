package com.gjh.shopdemo.message;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "notification")
public class NotificationProperties {

    private Map<String, String> channels = new HashMap<>();

    public List<String> getChannelsForEvent(String eventType) {
        String config = channels.get(eventType);
        if (config == null || config.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(config.trim().split("\\s*,\\s*"));
    }
}