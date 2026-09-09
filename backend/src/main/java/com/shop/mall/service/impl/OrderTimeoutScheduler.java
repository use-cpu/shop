package com.shop.mall.service.impl;

import com.shop.mall.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 订单超时自动关单定时任务
 * 每分钟执行一次, 扫描创建超过 30 分钟仍待支付的订单, 关闭并回补库存。
 *
 * @author shop-mall
 */
@Slf4j
@Component
public class OrderTimeoutScheduler {

    @Autowired
    private OrderService orderService;

    /** 每分钟执行一次 cron: 秒 分 时 日 月 周 */
    @Scheduled(cron = "0 * * * * ?")
    public void closeTimeout() {
        try {
            orderService.closeTimeoutOrders();
        } catch (Exception e) {
            log.error("超时关单任务异常", e);
        }
    }
}
