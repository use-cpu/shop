package com.shop.mall.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 购物车选中状态切换
 *
 * @author shop-mall
 */
@Data
public class CartSelectDTO {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "选中状态不能为空")
    private Boolean selected;
}
