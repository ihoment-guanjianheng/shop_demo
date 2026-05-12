package com.gjh.shopdemo.listener;

import com.gjh.shopdemo.constant.RedisConstant;
import com.gjh.shopdemo.pojo.exception.BaseException;
import com.gjh.shopdemo.pojo.mq.Message;
import com.gjh.shopdemo.pojo.mq.dto.StockUpdateMqDTO;
import com.gjh.shopdemo.service.SkuService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

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
    private SkuService skuService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public void onMessage(Message<StockUpdateMqDTO> message) {
        MDC.put("traceId", message.getMetadata().get("traceId"));
        String messageId = message.getMessageId();
        String key = RedisConstant.MQ_CONSUMED_KEY_PREFIX + messageId;
        try {
            Boolean ifAbsent = redisTemplate.opsForValue().setIfAbsent(key, "1", 24, TimeUnit.HOURS);
            if (Boolean.FALSE.equals(ifAbsent)) {
                log.warn("重复消费消息，跳过，messageId={}", messageId);
                return;
            }
            StockUpdateMqDTO dto = message.getPayload();
            skuService.deductDbStock(dto.getSkuId(), dto.getQuantity());
        } catch (BaseException e) {
            // 捕抓到业务异常则认为无法重试，直接进行告警处理
            log.error("处理库存消息失败，无法重试，请管理员进行手动处理，messageId={}", messageId, e);
            throw e;
        } catch (Exception e) {
            log.error("处理库存消息失败，重新入队，messageId={}", messageId, e);
            redisTemplate.delete(key);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}