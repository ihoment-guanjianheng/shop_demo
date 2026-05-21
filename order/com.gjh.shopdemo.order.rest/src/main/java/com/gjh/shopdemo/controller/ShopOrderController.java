package com.gjh.shopdemo.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gjh.shopdemo.context.AuthContext;
import com.gjh.shopdemo.pojo.dto.OrderCreateDTO;
import com.gjh.shopdemo.pojo.dto.OrderPageQueryDTO;
import com.gjh.shopdemo.pojo.model.ShopOrder;
import com.gjh.shopdemo.pojo.result.ShopResult;
import com.gjh.shopdemo.pojo.vo.OrderDetailVO;
import com.gjh.shopdemo.pojo.vo.UserInfoVO;
import com.gjh.shopdemo.service.ShopOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


/**
 * 订单管理接口
 */
@RestController
@RequestMapping("/shopOrder")
public class ShopOrderController {

    @Autowired
    private ShopOrderService shopOrderService;

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public ShopResult<Long> create(@RequestBody @Valid OrderCreateDTO dto) {
        return ShopResult.success(shopOrderService.createOrder(dto));
    }

    /**
     * 分页查询当前用户的订单列表
     */
    @GetMapping("/myPage")
    public ShopResult<IPage<ShopOrder>> myPage(OrderPageQueryDTO dto) {
        UserInfoVO currentUser = AuthContext.getCurrentUser();
        if (currentUser != null && currentUser.getId() != null) {
            dto.setUserId(currentUser.getId());
        }
        return ShopResult.success(shopOrderService.pageQuery(dto));
    }

    /**
     * 分页查询所有订单列表
     */
    @GetMapping("/page")
    public ShopResult<IPage<ShopOrder>> page(OrderPageQueryDTO dto) {
        return ShopResult.success(shopOrderService.pageQuery(dto));
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/{id}")
    public ShopResult<OrderDetailVO> detail(@PathVariable Long id) {
        return ShopResult.success(shopOrderService.detail(id));
    }

    /**
     * 取消订单
     */
    @PutMapping("/{id}/cancel")
    public ShopResult<Void> cancel(@PathVariable Long id, @RequestParam(required = false) String reason) {
        shopOrderService.cancelOrder(id, reason);
        return ShopResult.success();
    }

    /**
     * 支付订单
     */
    @PutMapping("/{id}/pay")
    public ShopResult<Void> pay(@PathVariable Long id) {
        shopOrderService.payOrder(id);
        return ShopResult.success();
    }

    /**
     * 订单发货
     */
    @PutMapping("/{id}/deliver")
    public ShopResult<Void> deliver(@PathVariable Long id) {
        shopOrderService.deliverOrder(id);
        return ShopResult.success();
    }

    /**
     * 确认收货
     */
    @PutMapping("/{id}/receive")
    public ShopResult<Void> receive(@PathVariable Long id) {
        shopOrderService.confirmReceive(id);
        return ShopResult.success();
    }
}