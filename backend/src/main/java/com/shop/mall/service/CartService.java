package com.shop.mall.service;

import com.shop.mall.dto.CartDTO;
import com.shop.mall.dto.CartSelectDTO;
import com.shop.mall.vo.CartVO;

import java.util.List;

/**
 * 购物车服务接口
 * 数据存于 Redis Hash, key=cart:{userId}, field=productId, value={qty,selected}
 *
 * @author shop-mall
 */
public interface CartService {

    /** 查看购物车(用户端) */
    CartVO list();

    /** 加入购物车 */
    void add(CartDTO dto);

    /** 修改数量 */
    void updateQuantity(CartDTO dto);

    /** 切换选中状态 */
    void toggleSelect(CartSelectDTO dto);

    /** 批量设置选中状态(全选/全不选) */
    void toggleSelectAll(Boolean selected);

    /** 删除购物车项 */
    void remove(List<Long> productIds);

    /** 离线购物车合并到用户购物车(登录触发) */
    void merge(String deviceToken);

    /** 清空选中项(下单后) */
    void clearSelected();

    /** 获取选中项商品ID列表 */
    List<Long> getSelectedProductIds();
}
