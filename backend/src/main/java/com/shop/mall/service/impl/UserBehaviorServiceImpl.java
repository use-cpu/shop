package com.shop.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.mall.entity.UserBehavior;
import com.shop.mall.mapper.UserBehaviorMapper;
import com.shop.mall.service.UserBehaviorService;
import com.shop.mall.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户行为服务实现(推荐数据源)
 * 唯一键 (user_id, product_id, behavior_type) 保证幂等, 重复浏览累加权重。
 *
 * @author shop-mall
 */
@Slf4j
@Service
public class UserBehaviorServiceImpl extends ServiceImpl<UserBehaviorMapper, UserBehavior>
        implements UserBehaviorService {

    /** 浏览权重 */
    private static final int WEIGHT_VIEW  = 1;
    /** 加购权重 */
    private static final int WEIGHT_CART = 3;

    @Override
    public void recordView(Long productId, Long categoryId) {
        record(productId, categoryId, 1, WEIGHT_VIEW);
    }

    @Override
    public void recordCart(Long productId, Long categoryId) {
        record(productId, categoryId, 2, WEIGHT_CART);
    }

    /**
     * 记录行为, 若已存在则权重累加
     */
    private void record(Long productId, Long categoryId, int type, int weight) {
        Long userId = UserContext.getUserIdNullable();
        if (userId == null) {
            // 未登录用户不记录行为, 推荐走热销兜底
            return;
        }
        UserBehavior exist = getOne(new LambdaQueryWrapper<UserBehavior>()
                .eq(UserBehavior::getUserId, userId)
                .eq(UserBehavior::getProductId, productId)
                .eq(UserBehavior::getBehaviorType, type)
                .last("LIMIT 1"));
        if (exist == null) {
            UserBehavior b = new UserBehavior();
            b.setUserId(userId);
            b.setProductId(productId);
            b.setCategoryId(categoryId);
            b.setBehaviorType(type);
            b.setWeight(weight);
            save(b);
        } else {
            exist.setWeight(exist.getWeight() + weight);
            exist.setUpdateTime(LocalDateTime.now());
            updateById(exist);
        }
    }
}
