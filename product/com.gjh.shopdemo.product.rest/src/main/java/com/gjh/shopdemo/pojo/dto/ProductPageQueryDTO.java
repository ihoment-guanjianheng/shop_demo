package com.gjh.shopdemo.pojo.dto;

import lombok.Data;

@Data
public class ProductPageQueryDTO {

    private Long userId;
    private String productName;
    private Integer status;
    private Long current = 1L;
    private Long size = 10L;
}