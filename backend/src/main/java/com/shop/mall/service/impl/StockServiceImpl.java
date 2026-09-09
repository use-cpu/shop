package com.shop.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.shop.mall.entity.Product;
import com.shop.mall.mapper.ProductMapper;
import com.shop.mall.service.StockService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存服务实现
 * 防超卖策略: Redis 原子 DECR + DB 乐观锁 双重保障
 *   1. Redis DECR 原子操作快速判定库存是否足够(挡住绝大多数并发)
 *   2. DB 层乐观锁 where version=old and stock>=qty 做最终一致性兜底
 *
 * @author shop-mall
 */
@Slf4j
@Service
public class StockServiceImpl implements StockService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ProductMapper productMapper;

    @Value("${shop.stock.prefix}")
    private String stockPrefix;

    /**
     * 服务启动时初始化全部在售商品库存到 Redis
     */
    @PostConstruct
    public void init() {
        // 延迟初始化, 确保 Spring 容器完全就绪后执行
        try {
            initAllStock();
        } catch (Exception e) {
            log.warn("启动初始化库存失败, 将按需加载: {}", e.getMessage());
        }
    }

    @Override
    public void initAllStock() {
        List<Product> products = productMapper.selectList(null);
        int count = 0;
        for (Product p : products) {
            redisTemplate.opsForValue().set(stockPrefix + p.getId(), p.getStock());
            count++;
        }
        log.info("库存缓存初始化完成, 共 {} 件商品", count);
    }

    @Override
    public void initStock(Long productId) {
        Product p = productMapper.selectById(productId);
        if (p == null) return;
        redisTemplate.opsForValue().set(stockPrefix + productId, p.getStock());
    }

    @Override
    public boolean deduct(Long productId, int quantity) {
        String key = stockPrefix + productId;
        // 1. Redis 原子扣减
        Long remain = redisTemplate.opsForValue().increment(key, -quantity);
        if (remain == null) {
            // 缓存不存在, 从 DB 加载后重试
            log.warn("库存缓存缺失, 加载 DB: productId={}", productId);
            initStock(productId);
            remain = redisTemplate.opsForValue().increment(key, -quantity);
        }
        if (remain < 0) {
            // 库存不足, 回滚 Redis
            redisTemplate.opsForValue().increment(key, quantity);
            return false;
        }
        // 2. DB 乐观锁兜底(更新条件: stock>=qty 且 version 匹配)
        Product curr = productMapper.selectById(productId);
        int affected = productMapper.update(null,
                new LambdaUpdateWrapper<Product>()
                        .eq(Product::getId, productId)
                        .eq(Product::getVersion, curr.getVersion())
                        .ge(Product::getStock, quantity)
                        .setSql("stock = stock - " + quantity)
                        .setSql("version = version + 1"));
        if (affected == 0) {
            // DB 层更新失败(并发被抢先), 回滚 Redis 并返回失败
            redisTemplate.opsForValue().increment(key, quantity);
            log.warn("DB 乐观锁更新失败, 回滚库存: productId={}, qty={}", productId, quantity);
            return false;
        }
        return true;
    }

    @Override
    public void rollback(Long productId, int quantity) {
        // 回补 Redis
        redisTemplate.opsForValue().increment(stockPrefix + productId, quantity);
        // 回补 DB
        productMapper.update(null,
                new LambdaUpdateWrapper<Product>()
                        .eq(Product::getId, productId)
                        .setSql("stock = stock + " + quantity)
                        .setSql("version = version + 1"));
    }

    @Override
    public int getStock(Long productId) {
        Object v = redisTemplate.opsForValue().get(stockPrefix + productId);
        if (v == null) {
            initStock(productId);
            v = redisTemplate.opsForValue().get(stockPrefix + productId);
        }
        if (v instanceof Number) return ((Number) v).intValue();
        return v == null ? 0 : Integer.parseInt(v.toString());
    }

    @Override
    public Map<Long, Integer> getStocks(String prefix) {
        Map<Long, Integer> map = new HashMap<>();
        // 该方法主要用于调试, 实际业务不依赖
        return map;
    }
}
