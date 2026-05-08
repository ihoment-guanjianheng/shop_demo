package com.gjh.shopdemo.pojo.dto;

import lombok.Data;

@Data
public class SkuPageQueryDTO {

    private Long productId;
    private Long current = 1L;
    private Long size = 10L;
}