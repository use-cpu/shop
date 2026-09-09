package com.shop.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.mall.entity.UserBehavior;

/**
 * 用户行为服务接口(推荐数据源)
 *
 * @author shop-mall
 */
public interface UserBehaviorService extends IService<UserBehavior> {

    /** 记录浏览行为(权重1) */
    void recordView(Long productId, Long categoryId);

    /** 记录加购行为(权重3) */
    void recordCart(Long productId, Long categoryId);
}
