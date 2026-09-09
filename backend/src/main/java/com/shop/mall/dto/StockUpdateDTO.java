package com.shop.mall.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 库存维护请求(管理后台)
 *
 * @author shop-mall
 */
@Data
public class StockUpdateDTO {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "库存数量不能为空")
    @Min(value = 0, message = "库存不能小于0")
    private Integer stock;
}
