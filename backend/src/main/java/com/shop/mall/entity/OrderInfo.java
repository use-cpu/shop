package com.shop.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单主表实体, 对应 order_info 表
 * 状态机: 0待支付 → 1已支付 → 2已发货 → 3已完成; 30min未支付 → 4已关闭
 *
 * @author shop-mall
 */
@Data
@TableName("order_info")
public class OrderInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private Integer totalQuantity;

    /** 0待支付1已支付2已发货3已完成4已关闭 */
    private Integer status;

    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;

    private LocalDateTime payTime;
    private LocalDateTime shipTime;
    private LocalDateTime finishTime;
    private LocalDateTime closeTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
