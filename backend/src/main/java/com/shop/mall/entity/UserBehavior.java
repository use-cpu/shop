package com.shop.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户行为实体, 对应 user_behavior 表
 * 用于个性化推荐: 记录用户浏览/加购商品的偏好权重。
 * 唯一键 (user_id, product_id, behavior_type) 用于行为幂等累加。
 *
 * @author shop-mall
 */
@Data
@TableName("user_behavior")
public class UserBehavior {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long productId;
    private Long categoryId;

    /** 1浏览 2加购 */
    private Integer behaviorType;

    /** 浏览=1 加购=3 */
    private Integer weight;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
