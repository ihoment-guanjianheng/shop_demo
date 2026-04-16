package com.gjh.shopdemo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjh.shopdemo.mapper.ShopOrderMapper;
import com.gjh.shopdemo.pojo.exception.BaseException;
import com.gjh.shopdemo.pojo.model.ShopOrder;
import com.gjh.shopdemo.service.ShopOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

@Service
@RefreshScope
@Slf4j
public class ShopOrderServiceImpl extends ServiceImpl<ShopOrderMapper, ShopOrder> implements ShopOrderService {

    @Value("${order.expirationTime}")
    private String expirationTime;

    @Value("${stock.threshold}")
    private String stockThreshold;

    @Value("${product.cacheTTL}")
    private String productCacheTTL;

    @Override
    public String testNacos() {
        log.info("订单超时时长: {}, 库存预警阈值: {}, 商品缓存TTL: {}", expirationTime, stockThreshold, productCacheTTL);
        return String.format("订单超时时长: %s, 库存预警阈值: %s, 商品缓存TTL: %s", expirationTime, stockThreshold, productCacheTTL);
    }
}