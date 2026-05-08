package com.gjh.shopdemo.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class ProductUpdateDTO {

    @NotNull(message = "商品ID不能为空")
    private Long id;

    @Size(max = 64, message = "商品编码长度不能超过64")
    private String productCode;

    @Size(max = 128, message = "商品名称长度不能超过128")
    private String productName;

    private String description;

    @Size(max = 255, message = "主图URL长度不能超过255")
    private String mainImage;

    private Integer status;
}