package com.gjh.shopdemo.notify;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {

    private String eventType;

    private String title;

    private String content;

    private List<String> receivers;
}