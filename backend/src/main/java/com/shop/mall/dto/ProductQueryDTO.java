package com.shop.mall.dto;

import lombok.Data;

/**
 * 商品分页查询参数
 *
 * @author shop-mall
 */
@Data
public class ProductQueryDTO {

    /** 关键词搜索: 匹配 name/subtitle */
    private String keyword;

    /** 分类ID过滤 */
    private Long categoryId;

    /** 排序字段: price_asc / price_desc / sales_desc / default */
    private String sort = "default";

    private Integer pageNum = 1;
    private Integer pageSize = 12;
}
