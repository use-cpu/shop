package com.shop.mall.service;

import com.shop.mall.vo.RecommendVO;

/**
 * 个性化推荐服务接口
 *
 * @author shop-mall
 */
public interface RecommendService {

    /**
     * 首页推荐聚合
     * 1. 已登录且有行为数据: 基于用户偏好分类推荐未浏览商品
     * 2. 新用户或无行为: 回退到全平台热销榜
     */
    RecommendVO homeRecommend();
}
