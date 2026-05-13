package com.gjh.shopdemo.listener.handler;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StockTraceIdHandler extends AbstractStockUpdateHandler {
    @Override
    public void handle(StockUpdateContext ctx) {
        MDC.put("traceId", ctx.getTraceId());
        passToNext(ctx);
    }
}
