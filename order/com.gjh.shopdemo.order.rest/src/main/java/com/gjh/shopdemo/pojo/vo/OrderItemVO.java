package com.gjh.shopdemo.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemVO {

    private Long id;
    private Long orderId;
    private Long productId;
    private Long skuId;
    private String skuName;
    private String skuImage;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal totalAmount;
}