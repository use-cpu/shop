package com.shop.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.mall.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品 Mapper
 *
 * @author shop-mall
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
