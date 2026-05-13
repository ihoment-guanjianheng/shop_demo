package com.gjh.shopdemo.listener.handler;

import com.gjh.shopdemo.pojo.exception.BaseException;
import com.gjh.shopdemo.pojo.mq.dto.StockUpdateMqDTO;
import com.gjh.shopdemo.service.SkuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class StockDispatchHandler extends AbstractStockUpdateHandler {

    @Autowired
    private SkuService skuService;

    @Override
    public void handle(StockUpdateContext ctx) {
        StockUpdateMqDTO dto = ctx.getDto();
        skuService.deductDbStock(dto.getSkuId(), dto.getQuantity());
    }
}