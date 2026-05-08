package com.gjh.shopdemo.pojo.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class OrderCreateItemDTO {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "SKU ID不能为空")
    private Long skuId;

    private String skuName;

    private String skuImage;

    @NotNull(message = "单价不能为空")
    private BigDecimal unitPrice;

    @NotNull(message = "购买数量不能为空")
    @Min(value = 1, message = "购买数量必须大于0")
    private Integer quantity;
}