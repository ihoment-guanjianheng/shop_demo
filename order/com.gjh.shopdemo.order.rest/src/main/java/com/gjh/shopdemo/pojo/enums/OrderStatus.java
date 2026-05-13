package com.gjh.shopdemo.pojo.enums;

import com.gjh.shopdemo.pojo.exception.BaseException;

import java.util.EnumSet;
import java.util.Set;

public enum OrderStatus {

    PENDING_PAYMENT(0, "待支付"),
    PAID(1, "已支付"),
    SHIPPED(2, "已发货"),
    COMPLETED(3, "已完成"),
    CANCELLED(4, "已取消");

    public final int code;
    public final String desc;

    private Set<OrderStatus> allowedTransitions;

    OrderStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    static {
        PENDING_PAYMENT.allowedTransitions = EnumSet.of(PAID, CANCELLED);
        PAID.allowedTransitions             = EnumSet.of(SHIPPED);
        SHIPPED.allowedTransitions          = EnumSet.of(COMPLETED);
        COMPLETED.allowedTransitions        = EnumSet.noneOf(OrderStatus.class);
        CANCELLED.allowedTransitions        = EnumSet.noneOf(OrderStatus.class);
    }

    public void checkTransition(OrderStatus target) {
        if (!allowedTransitions.contains(target)) {
            throw new BaseException(
                    String.format("订单状态不允许从 [%s] 转为 [%s]", this.desc, target.desc));
        }
    }

    public static OrderStatus of(Integer code) {
        if (code == null) {
            throw new BaseException("订单状态不能为空");
        }
        for (OrderStatus s : values()) {
            if (s.code == code) {
                return s;
            }
        }
        throw new BaseException("未知的订单状态: " + code);
    }
}