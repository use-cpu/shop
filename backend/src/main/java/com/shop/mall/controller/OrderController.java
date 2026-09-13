package com.shop.mall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shop.mall.common.Result;
import com.shop.mall.dto.BuyNowDTO;
import com.shop.mall.dto.CheckoutDTO;
import com.shop.mall.entity.OrderInfo;
import com.shop.mall.service.OrderService;
import com.shop.mall.vo.OrderVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 订单 Controller
 *
 * @author shop-mall
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /** 创建订单(从购物车选中项, 扣库存) */
    @PostMapping
    public Result<OrderInfo> create(@Valid @RequestBody CheckoutDTO dto) {
        return Result.success(orderService.createOrder(dto));
    }

    /** 立即购买(跳过购物车直接下单) */
    @PostMapping("/buy-now")
    public Result<OrderInfo> buyNow(@Valid @RequestBody BuyNowDTO dto) {
        return Result.success(orderService.buyNow(dto));
    }

    /** 模拟支付回调 */
    @PostMapping("/pay/{orderNo}")
    public Result<Void> pay(@PathVariable String orderNo) {
        orderService.pay(orderNo);
        return Result.success();
    }

    /** 取消订单 */
    @PostMapping("/cancel/{orderNo}")
    public Result<Void> cancel(@PathVariable String orderNo) {
        orderService.cancel(orderNo);
        return Result.success();
    }

    /** 确认收货 */
    @PostMapping("/confirm/{orderNo}")
    public Result<Void> confirm(@PathVariable String orderNo) {
        orderService.confirm(orderNo);
        return Result.success();
    }

    @DeleteMapping("/{orderNo}")
    public Result<Void> delete(@PathVariable String orderNo) {
        orderService.delete(orderNo);
        return Result.success();
    }

    /** 订单详情 */
    @GetMapping("/{orderNo}")
    public Result<OrderVO> detail(@PathVariable String orderNo) {
        return Result.success(orderService.detail(orderNo));
    }

    /** 我的订单 */
    @GetMapping("/list")
    public Result<IPage<OrderVO>> myOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(orderService.myOrders(status, pageNum, pageSize));
    }

    /* ==================== 管理后台 ==================== */
    @GetMapping("/admin/list")
    public Result<IPage<OrderVO>> adminList(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(orderService.allOrders(status, pageNum, pageSize));
    }

    @PostMapping("/admin/ship/{orderNo}")
    public Result<Void> ship(@PathVariable String orderNo) {
        orderService.ship(orderNo);
        return Result.success();
    }
}
