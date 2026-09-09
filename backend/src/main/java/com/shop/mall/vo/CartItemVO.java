package com.shop.mall.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 购物车单项视图
 *
 * @author shop-mall
 */
@Data
public class CartItemVO {

    private Long productId;
    private String productName;
    private String mainImage;
    private BigDecimal price;
    private Integer stock;
    private Integer quantity;
    private Boolean selected;

    /** 小计金额 = price * quantity */
    private BigDecimal subtotal;
}
