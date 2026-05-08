package com.gjh.shopdemo.pojo.dto;

import lombok.Data;

@Data
public class OrderPageQueryDTO {

    private Long userId;

    private Integer status;

    private String orderNo;

    private Long current = 1L;

    private Long size = 10L;
}