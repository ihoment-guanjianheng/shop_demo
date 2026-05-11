package com.gjh.shopdemo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gjh.shopdemo.context.AuthContext;
import com.gjh.shopdemo.mapper.ShopOrderMapper;
import com.gjh.shopdemo.pojo.dto.OrderCreateDTO;
import com.gjh.shopdemo.pojo.dto.OrderCreateItemDTO;
import com.gjh.shopdemo.pojo.dto.OrderPageQueryDTO;
import com.gjh.shopdemo.pojo.exception.BaseException;
import com.gjh.shopdemo.pojo.model.OrderItem;
import com.gjh.shopdemo.pojo.model.ShopOrder;
import com.gjh.shopdemo.pojo.vo.OrderDetailVO;
import com.gjh.shopdemo.pojo.vo.OrderItemVO;
import com.gjh.shopdemo.pojo.vo.UserInfoVO;
import com.gjh.shopdemo.service.OrderItemService;
import com.gjh.shopdemo.service.ShopOrderService;
import com.gjh.shopdemo.strategy.StockStrategy;
import com.gjh.shopdemo.util.UUIDUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RefreshScope
@Slf4j
public class ShopOrderServiceImpl extends ServiceImpl<ShopOrderMapper, ShopOrder> implements ShopOrderService {

    @Value("${order.expirationTime}")
    private Long expirationTime;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private StockStrategy stockStrategy;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(OrderCreateDTO dto) {
        UserInfoVO currentUser = AuthContext.getCurrentUser();
        if (currentUser == null || currentUser.getId() == null) {
            throw new BaseException("用户未登录");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderCreateItemDTO itemDTO : dto.getItems()) {
            BigDecimal itemTotal = itemDTO.getUnitPrice().multiply(BigDecimal.valueOf(itemDTO.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(itemDTO.getProductId());
            orderItem.setSkuId(itemDTO.getSkuId());
            orderItem.setSkuName(itemDTO.getSkuName());
            orderItem.setSkuImage(itemDTO.getSkuImage());
            orderItem.setUnitPrice(itemDTO.getUnitPrice());
            orderItem.setQuantity(itemDTO.getQuantity());
            orderItem.setTotalAmount(itemTotal);
            orderItems.add(orderItem);
        }

        BigDecimal discountAmount = dto.getDiscountAmount() == null ? BigDecimal.ZERO : dto.getDiscountAmount();
        BigDecimal freightAmount = dto.getFreightAmount() == null ? BigDecimal.ZERO : dto.getFreightAmount();
        BigDecimal payAmount = totalAmount.subtract(discountAmount).add(freightAmount);
        if (payAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BaseException("订单实付金额不能为负数");
        }

        List<Long> preoccupySkuIds = new ArrayList<>();
        List<Integer> preoccupyQuantities = new ArrayList<>();
        for (OrderCreateItemDTO itemDTO : dto.getItems()) {
            boolean success = stockStrategy.preoccupy(itemDTO.getSkuId(), itemDTO.getQuantity());
            if (!success) {
                for (int i = 0; i < preoccupySkuIds.size(); i++) {
                    stockStrategy.release(preoccupySkuIds.get(i), preoccupyQuantities.get(i));
                }
                throw new BaseException("商品库存不足或库存未初始化，SKU ID: " + itemDTO.getSkuId());
            }
            preoccupySkuIds.add(itemDTO.getSkuId());
            preoccupyQuantities.add(itemDTO.getQuantity());
        }

        ShopOrder order = new ShopOrder();
        order.setOrderNo(UUIDUtils.getUUID());
        order.setUserId(currentUser.getId());
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setFreightAmount(freightAmount);
        order.setPayAmount(payAmount);
        order.setStatus(0);
        order.setPayType(dto.getPayType());
        order.setRemark(dto.getRemark());
        save(order);

        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(order.getId());
        }
        orderItemService.saveBatch(orderItems);
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        log.info("订单创建成功，发送延迟取消消息，orderNo: {}", order.getOrderNo());
                        boolean result = stockStrategy.sendDelayOrderCreatedMessage(order, expirationTime);
                        if(!result){
                            log.error("发送延迟取消消息失败，orderNo: {}", order.getOrderNo());
                            //TODO db补偿策略
                        }
                    }
                }
        );
        return order.getId();
    }

    @Override
    public IPage<ShopOrder> pageQuery(OrderPageQueryDTO dto) {
        Page<ShopOrder> page = new Page<>(dto.getCurrent(), dto.getSize());
        LambdaQueryWrapper<ShopOrder> wrapper = new LambdaQueryWrapper<>();
        if (dto.getUserId() != null) {
            wrapper.eq(ShopOrder::getUserId, dto.getUserId());
        }
        if (dto.getStatus() != null) {
            wrapper.eq(ShopOrder::getStatus, dto.getStatus());
        }
        if (StringUtils.hasText(dto.getOrderNo())) {
            wrapper.like(ShopOrder::getOrderNo, dto.getOrderNo());
        }
        wrapper.orderByDesc(ShopOrder::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public OrderDetailVO detail(Long id) {
        ShopOrder order = getById(id);
        if (order == null) {
            throw new BaseException("订单不存在");
        }
        OrderDetailVO vo = new OrderDetailVO();
        BeanUtils.copyProperties(order, vo);

        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, id);
        List<OrderItem> items = orderItemService.list(wrapper);
        List<OrderItemVO> itemVOs = items.stream().map(item -> {
            OrderItemVO itemVO = new OrderItemVO();
            BeanUtils.copyProperties(item, itemVO);
            return itemVO;
        }).collect(Collectors.toList());
        vo.setItems(itemVOs);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long id, String reason) {
        ShopOrder order = getById(id);
        if (order == null) {
            throw new BaseException("订单不存在");
        }
        if (!Integer.valueOf(0).equals(order.getStatus())) {
            throw new BaseException("当前订单状态不允许取消");
        }

        // 释放库存
        releaseOrderStock(id);

        LambdaUpdateWrapper<ShopOrder> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ShopOrder::getId, id)
                .set(ShopOrder::getStatus, 4)
                .set(ShopOrder::getCancelTime, LocalDateTime.now())
                .set(ShopOrder::getCancelReason, reason);
        update(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long id) {
        ShopOrder order = getById(id);
        if (order == null) {
            throw new BaseException("订单不存在");
        }
        if (!Integer.valueOf(0).equals(order.getStatus())) {
            throw new BaseException("当前订单状态不允许支付");
        }

        // 确认扣减库存
        confirmOrderStock(id);

        LambdaUpdateWrapper<ShopOrder> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ShopOrder::getId, id)
                .set(ShopOrder::getStatus, 1)
                .set(ShopOrder::getPayTime, LocalDateTime.now());
        update(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deliverOrder(Long id) {
        ShopOrder order = getById(id);
        if (order == null) {
            throw new BaseException("订单不存在");
        }
        if (!Integer.valueOf(1).equals(order.getStatus())) {
            throw new BaseException("当前订单状态不允许发货");
        }
        LambdaUpdateWrapper<ShopOrder> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ShopOrder::getId, id)
                .set(ShopOrder::getStatus, 2)
                .set(ShopOrder::getDeliveryTime, LocalDateTime.now());
        update(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceive(Long id) {
        ShopOrder order = getById(id);
        if (order == null) {
            throw new BaseException("订单不存在");
        }
        if (!Integer.valueOf(2).equals(order.getStatus())) {
            throw new BaseException("当前订单状态不允许确认收货");
        }
        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<ShopOrder> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ShopOrder::getId, id)
                .set(ShopOrder::getStatus, 3)
                .set(ShopOrder::getReceiveTime, now)
                .set(ShopOrder::getFinishTime, now);
        update(wrapper);
    }

    private void confirmOrderStock(Long orderId) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemService.list(wrapper);
        for (OrderItem item : items) {
            stockStrategy.confirm(item.getSkuId(), item.getQuantity());
        }
    }

    private void releaseOrderStock(Long orderId) {
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemService.list(wrapper);
        for (OrderItem item : items) {
            stockStrategy.release(item.getSkuId(), item.getQuantity());
        }
    }
}