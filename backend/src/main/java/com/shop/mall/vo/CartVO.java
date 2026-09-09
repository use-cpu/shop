package com.shop.mall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 购物车聚合视图
 *
 * @author shop-mall
 */
@Data
public class CartVO {

    private List<CartItemVO> items;
    private Integer totalCount;
    private Integer selectedCount;

    /** 选中项总金额 */
    private BigDecimal selectedAmount;
}
