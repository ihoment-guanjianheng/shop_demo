package com.gjh.shopdemo.listener;

import com.gjh.shopdemo.listener.handler.*;
import com.gjh.shopdemo.pojo.exception.BaseException;
import com.gjh.shopdemo.pojo.mq.Message;
import com.gjh.shopdemo.pojo.mq.dto.StockUpdateMqDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
@RocketMQMessageListener(
        topic = "order_create",
        consumerGroup = "stock_update_consumer",
        consumeMode = ConsumeMode.ORDERLY,
        consumeThreadNumber = 8,
        maxReconsumeTimes = 5
)
public class StockUpdateListener implements RocketMQListener<Message<StockUpdateMqDTO>> {

    @Autowired
    private StockTraceIdHandler stockTraceIdHandler;
    @Autowired
    private StockIdempotencyHandler stockIdempotencyHandler;
    @Autowired
    private StockDispatchHandler stockDispatchHandler;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private AbstractStockUpdateHandler chain;

    @PostConstruct
    private void buildChain() {
        stockTraceIdHandler
                .setNext(stockIdempotencyHandler)
                .setNext(stockDispatchHandler);
        chain = stockIdempotencyHandler;
    }

    @Override
    public void onMessage(Message<StockUpdateMqDTO> message) {
        StockUpdateContext ctx = new StockUpdateContext(message);
        try {
            chain.handle(ctx);
        } catch (BaseException e) {
            log.error("处理库存消息失败，无法重试，请管理员进行手动处理，messageId={}", ctx.getMessageId(), e);
            throw e;
        } catch (Exception e) {
            log.error("处理库存消息失败，重新入队，messageId={}", ctx.getMessageId(), e);
            redisTemplate.delete(ctx.getRedisKey());
            throw e;
        } finally {
            MDC.clear();
        }
    }
}