package com.gjh.shopdemo.pojo.mq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateMqDTO {
    private Long skuId;
    private Integer quantity;
}