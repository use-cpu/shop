package com.shop.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体, 对应 product 表
 * version 字段配合 MyBatis-Plus 乐观锁, 防止并发超卖。
 *
 * @author shop-mall
 */
@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long categoryId;
    private String name;
    private String subtitle;
    private String mainImage;
    private String images;
    private String detail;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private Integer sales;

    /** 0下架 1上架 */
    private Integer status;

    /** 乐观锁版本号 */
    @Version
    private Integer version;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
