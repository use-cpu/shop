package com.shop.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品分类实体, 对应 category 表
 * parent_id=0 表示一级分类; 非零为其父分类ID(支持二级以上)。
 *
 * @author shop-mall
 */
@Data
@TableName("category")
public class Category {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private Long parentId;
    private Integer sort;
    private String icon;

    /** 0禁用 1正常 */
    private Integer status;

    private LocalDateTime createTime;
}
