package com.gjh.shopdemo.pojo.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateDTO {

    private BigDecimal discountAmount;

    private BigDecimal freightAmount;

    private Integer payType;

    private String remark;

    @NotEmpty(message = "订单商品不能为空")
    @Valid
    private List<OrderCreateItemDTO> items;
}