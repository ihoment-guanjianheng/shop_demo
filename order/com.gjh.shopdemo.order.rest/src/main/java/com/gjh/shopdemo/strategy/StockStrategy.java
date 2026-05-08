package com.gjh.shopdemo.strategy;

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
}