package com.shop.mall.vo;

import lombok.Data;

import java.util.List;

/**
 * 首页个性化推荐聚合
 *
 * @author shop-mall
 */
@Data
public class RecommendVO {

    /** 个性化推荐(基于用户行为) */
    private List<ProductVO> personalized;

    /** 平台热销榜(兜底/新用户) */
    private List<ProductVO> hot;

    /** 分类入口(一级) */
    private List<CategoryVO> categories;
}
