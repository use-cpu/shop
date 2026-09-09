package com.shop.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop.mall.entity.OrderInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单主表 Mapper
 *
 * @author shop-mall
 */
@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {
}
