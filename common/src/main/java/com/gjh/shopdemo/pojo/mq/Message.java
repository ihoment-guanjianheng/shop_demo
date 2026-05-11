package com.gjh.shopdemo.pojo.mq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message<T> {
    private String messageId;        // 事件唯一 ID（幂等键）
    private String messageType;      // 事件类型：order.created
    private Long aggregateId;    // 聚合根 ID（如 orderId）
    private Long timestamp;        // 发生时间戳
    private Integer version;       // 事件版本
    private T payload;             // 业务数据
    private Map<String, String> metadata; // 扩展字段
}
