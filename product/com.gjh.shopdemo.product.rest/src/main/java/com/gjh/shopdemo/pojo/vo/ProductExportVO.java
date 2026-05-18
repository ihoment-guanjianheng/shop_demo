package com.gjh.shopdemo.pojo.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ProductExportVO {

    @ExcelProperty("商品编码")
    private String productCode;

    @ExcelProperty("商品名称")
    private String productName;

    @ExcelProperty("描述")
    private String description;

    @ExcelProperty("主图URL")
    private String mainImage;

    @ExcelProperty("状态")
    private String status;

    @ExcelProperty("创建时间")
    private String createTime;
}
