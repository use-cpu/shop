package com.shop.mall.service;

import java.util.Map;

/**
 * 库存服务接口
 * 采用 Redis 原子扣减 + DB 乐观锁 双重保障, 防止并发超卖。
 *
 * @author shop-mall
 */
public interface StockService {

    /**
     * 扣减库存(Redis原子DECR + DB乐观锁)
     * @return 成功 true; 失败(库存不足) false
     */
    boolean deduct(Long productId, int quantity);

    /**
     * 回补库存(关单/取消)
     */
    void rollback(Long productId, int quantity);

    /**
     * 初始化商品库存到Redis(服务启动/商品上架)
     */
    void initStock(Long productId);

    /**
     * 批量初始化库存
     */
    void initAllStock();

    /**
     * 获取当前Redis缓存的库存
     */
    int getStock(Long productId);

    /**
     * 批量初始化/查询库存
     */
    Map<Long, Integer> getStocks(String prefix);
}
