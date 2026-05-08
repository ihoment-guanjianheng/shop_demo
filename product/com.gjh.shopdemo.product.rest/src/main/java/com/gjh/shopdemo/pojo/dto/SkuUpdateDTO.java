package com.gjh.shopdemo.pojo.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
public class SkuUpdateDTO {

    @NotNull(message = "SKU ID不能为空")
    private Long id;

    @Size(max = 64, message = "SKU编码长度不能超过64")
    private String skuCode;

    @Size(max = 128, message = "SKU名称长度不能超过128")
    private String skuName;

    private String skuSpecs;

    @DecimalMin(value = "0.01", message = "售价必须大于0")
    private BigDecimal price;

    @Min(value = 0, message = "库存不能小于0")
    private Integer stock;

    @Min(value = 0, message = "锁定库存不能小于0")
    private Integer lockStock;

    @Size(max = 255, message = "图片URL长度不能超过255")
    private String image;

    private Integer status;
}