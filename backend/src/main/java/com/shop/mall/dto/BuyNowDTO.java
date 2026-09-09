package com.shop.mall.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 立即购买请求(跳过购物车直接下单)
 *
 * @author shop-mall
 */
@Data
public class BuyNowDTO {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;

    @NotNull(message = "请选择收货地址")
    private Long addressId;
}
