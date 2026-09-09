package com.shop.mall.vo;

import com.shop.mall.entity.OrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单视图(含明细)
 *
 * @author shop-mall
 */
@Data
public class OrderVO {

    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private Integer totalQuantity;

    /** 0待支付1已支付2已发货3已完成4已关闭 */
    private Integer status;
    private String statusText;

    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;

    private LocalDateTime payTime;
    private LocalDateTime shipTime;
    private LocalDateTime finishTime;
    private LocalDateTime closeTime;
    private LocalDateTime createTime;

    private List<OrderItem> orderItems;
}
