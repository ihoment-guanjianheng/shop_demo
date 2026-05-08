package com.gjh.shopdemo.pojo.vo;

import com.gjh.shopdemo.pojo.model.Sku;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductDetailVO {

    private Long id;
    private String productCode;
    private String productName;
    private String description;
    private String mainImage;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<Sku> skuList;
}