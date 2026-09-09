package com.shop.mall.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 购物车操作请求
 *
 * @author shop-mall
 */
@Data
public class CartDTO {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;

    /** 离线购物车合并令牌(未登录时前端生成UUID) */
    private String deviceToken;
}
