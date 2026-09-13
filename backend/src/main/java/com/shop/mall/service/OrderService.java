package com.shop.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shop.mall.dto.BuyNowDTO;
import com.shop.mall.dto.CheckoutDTO;
import com.shop.mall.entity.OrderInfo;
import com.shop.mall.vo.OrderVO;

/**
 * 订单服务接口
 *
 * @author shop-mall
 */
public interface OrderService {

    /** 创建订单(从购物车选中项, 扣库存) */
    OrderInfo createOrder(CheckoutDTO dto);

    /** 立即购买(跳过购物车, 直接下单单件商品) */
    OrderInfo buyNow(BuyNowDTO dto);

    /** 模拟支付回调 */
    void pay(String orderNo);

    /** 取消订单 */
    void cancel(String orderNo);

    /** 确认收货 */
    void confirm(String orderNo);

    /** 删除订单记录(仅已完成/已关闭) */
    void delete(String orderNo);

    /** 订单详情 */
    OrderVO detail(String orderNo);

    /** 我的订单(用户端) */
    IPage<OrderVO> myOrders(Integer status, int pageNum, int pageSize);

    /** 全部订单(管理后台) */
    IPage<OrderVO> allOrders(Integer status, int pageNum, int pageSize);

    /** 管理员发货 */
    void ship(String orderNo);

    /** 定时任务: 关闭超时未支付订单 */
    void closeTimeoutOrders();
}
