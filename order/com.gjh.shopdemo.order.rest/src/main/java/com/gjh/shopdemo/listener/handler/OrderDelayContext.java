package com.gjh.shopdemo.listener.handler;

import com.gjh.shopdemo.constant.RedisConstant;
import com.gjh.shopdemo.pojo.model.ShopOrder;
import com.gjh.shopdemo.pojo.mq.Message;
import lombok.Getter;
import lombok.Setter;

@Getter
public class OrderDelayContext {

    private final String messageId;
    private final String redisKey;
    private final Long orderId;

    @Setter
    private ShopOrder order;

    public OrderDelayContext(Message<ShopOrder> message) {
        this.messageId = message.getMessageId();
        this.redisKey = RedisConstant.MQ_CONSUMED_KEY_PREFIX + messageId;
        this.orderId = message.getPayload().getId();
    }
}