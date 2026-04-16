package com.gjh.shopdemo.pojo.enums;

import lombok.Getter;

/**
 * 返回结果枚举
 */

@Getter
public enum ResultEnum {
    SUCCESS(1, "成功"),
    FAIL(0, "失败"),
    SERVICE_NOT_ACCESS(2, "服务不可达");

    private Integer code;
    private String message;

    ResultEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
