package com.shop.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.mall.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分类 Mapper
 *
 * @author shop-mall
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
