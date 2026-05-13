package com.gjh.shopdemo.listener.handler;

import com.gjh.shopdemo.constant.RedisConstant;
import com.gjh.shopdemo.pojo.mq.Message;
import com.gjh.shopdemo.pojo.mq.dto.StockUpdateMqDTO;
import lombok.Getter;

@Getter
public class StockUpdateContext {

    private final String messageId;
    private final String redisKey;
    private final String traceId;
    private final StockUpdateMqDTO dto;

    public StockUpdateContext(Message<StockUpdateMqDTO> message) {
        this.messageId = message.getMessageId();
        this.redisKey = RedisConstant.MQ_CONSUMED_KEY_PREFIX + messageId;
        this.traceId = message.getMetadata().get("traceId");
        this.dto = message.getPayload();
    }
}