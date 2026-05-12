package com.gjh.shopdemo.strategy;

import com.gjh.shopdemo.pojo.model.OrderItem;
import com.gjh.shopdemo.pojo.model.ShopOrder;

import java.util.List;

/**
 * 库存扣减策略接口
 */
public interface StockStrategy {

    /**
     * 预占/扣减库存（创建订单时调用）
     *
     * @param skuId    SKU ID
     * @param quantity 数量
     * @return true: 成功; false: 库存不足
     */
    boolean preoccupy(Long skuId, Integer quantity);

    /**
     * 确认扣减（支付成功时调用）
     *
     * @param skuId    SKU ID
     * @param quantity 数量
     * @return true: 成功; false: 失败
     */
    boolean confirm(Long skuId, Integer quantity);

    /**
     * 释放库存（取消/超时未支付时调用）
     *
     * @param skuId    SKU ID
     * @param quantity 数量
     * @return true: 成功
     */
    boolean release(Long skuId, Integer quantity);

    /**
     * 发送延迟消息, 订单创建时调用, 用于在订单超时未支付时释放库存
     */
    boolean sendDelayOrderCreatedMessage(ShopOrder shopOrder, Long delayTime);

    /**
     * 支付成功事务提交后的后置动作（afterCommit 中调用）。
     * 预占策略：通过 MQ 异步通知 Product 服务扣减 DB 库存。
     * 立即扣减策略：无操作（DB 已在 preoccupy 时同步扣减）。
     */
    default void afterPayCommit(List<OrderItem> items) {
    }
}