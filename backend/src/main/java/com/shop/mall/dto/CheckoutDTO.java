package com.shop.mall.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 下单请求
 *
 * @author shop-mall
 */
@Data
public class CheckoutDTO {

    /** 收货地址ID */
    @NotNull(message = "请选择收货地址")
    private Long addressId;

    /** 离线购物车令牌(未登录下单场景, 本系统要求登录) */
    private String deviceToken;
}
