package com.shop.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.mall.entity.UserBehavior;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 用户行为 Mapper
 * 提供个性化推荐所需的聚合查询。
 *
 * @author shop-mall
 */
@Mapper
public interface UserBehaviorMapper extends BaseMapper<UserBehavior> {

    /**
     * 统计用户各分类的偏好权重之和, 按权重降序。
     * 用于"偏好分类 TopN"推荐。
     */
    @Select("SELECT category_id, SUM(weight) AS total_weight " +
            "FROM user_behavior " +
            "WHERE user_id = #{userId} " +
            "GROUP BY category_id " +
            "ORDER BY total_weight DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectCategoryWeights(Long userId, int limit);
}
