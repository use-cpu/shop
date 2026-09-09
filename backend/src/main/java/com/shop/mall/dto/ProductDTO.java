package com.shop.mall.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品新增/修改请求(管理后台用)
 *
 * @author shop-mall
 */
@Data
public class ProductDTO {

    private Long id;

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @NotNull(message = "商品名不能为空")
    private String name;

    private String subtitle;
    private String mainImage;
    private String images;
    private String detail;

    @NotNull(message = "售价不能为空")
    private BigDecimal price;

    private BigDecimal originalPrice;
    private Integer stock;

    /** 0下架 1上架 */
    private Integer status;
}
