package com.shop.mall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品详情/列表展示
 *
 * @author shop-mall
 */
@Data
public class ProductVO {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String subtitle;
    private String mainImage;
    private List<String> images;
    private String detail;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private Integer sales;
    private Integer status;
    private Integer version;
}
