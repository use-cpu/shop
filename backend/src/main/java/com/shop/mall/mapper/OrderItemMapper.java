package com.shop.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.mall.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单明细 Mapper
 *
 * @author shop-mall
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
