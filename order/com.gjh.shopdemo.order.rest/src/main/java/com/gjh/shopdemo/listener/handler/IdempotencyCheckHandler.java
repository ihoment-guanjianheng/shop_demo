package com.gjh.shopdemo.listener.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class IdempotencyCheckHandler extends AbstractOrderDelayHandler {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void handle(OrderDelayContext ctx) {
        Boolean ifAbsent = redisTemplate.opsForValue()
                .setIfAbsent(ctx.getRedisKey(), "1", 24, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(ifAbsent)) {
            log.warn("重复消费消息，跳过处理，messageId={}", ctx.getMessageId());
            return;
        }
        passToNext(ctx);
    }
}