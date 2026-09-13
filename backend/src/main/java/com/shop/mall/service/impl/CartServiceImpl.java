package com.shop.mall.service.impl;

import com.shop.mall.common.BusinessException;
import com.shop.mall.common.ResultCode;
import com.shop.mall.dto.CartDTO;
import com.shop.mall.dto.CartSelectDTO;
import com.shop.mall.entity.Product;
import com.shop.mall.mapper.ProductMapper;
import com.shop.mall.service.CartService;
import com.shop.mall.service.StockService;
import com.shop.mall.service.UserBehaviorService;
import com.shop.mall.utils.UserContext;
import com.shop.mall.vo.CartItemVO;
import com.shop.mall.vo.CartVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 购物车服务实现(Redis Hash 存储)
 * Key: cart:{userId} → Hash{ field=productId, value={qty,selected} JSON }
 * 离线购物车: cart:temp:{deviceToken}
 *
 * @author shop-mall
 */
@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private StockService stockService;

    @Autowired
    private UserBehaviorService userBehaviorService;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Value("${shop.cart.user-prefix}")
    private String userPrefix;

    @Value("${shop.cart.temp-prefix}")
    private String tempPrefix;

    @Override
    public CartVO list() {
        String key = userKey();
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        List<CartItemVO> items = new ArrayList<>();
        int total = 0;
        int selectedCount = 0;
        BigDecimal selectedAmount = BigDecimal.ZERO;

        // 批量查询购物车商品, 避免循环内逐条 selectById 造成 N+1
        List<Long> productIds = entries.keySet().stream()
                .map(k -> Long.valueOf(k.toString())).collect(Collectors.toList());
        Map<Long, Product> productMap = productIds.isEmpty()
                ? Map.of()
                : productMapper.selectBatchIds(productIds).stream()
                        .collect(Collectors.toMap(Product::getId, p -> p));

        for (Map.Entry<Object, Object> e : entries.entrySet()) {
            Long productId = Long.valueOf(e.getKey().toString());
            CartItem item = parseItem(e.getValue());
            Product p = productMap.get(productId);
            if (p == null) continue;

            CartItemVO vo = new CartItemVO();
            vo.setProductId(p.getId());
            vo.setProductName(p.getName());
            vo.setMainImage(p.getMainImage());
            vo.setPrice(p.getPrice());
            vo.setStock(p.getStock());
            vo.setQuantity(item.getQuantity());
            vo.setSelected(item.getSelected());
            vo.setSubtotal(p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            items.add(vo);

            total += item.getQuantity();
            if (item.getSelected()) {
                selectedCount += item.getQuantity();
                selectedAmount = selectedAmount.add(vo.getSubtotal());
            }
        }
        CartVO cart = new CartVO();
        cart.setItems(items);
        cart.setTotalCount(total);
        cart.setSelectedCount(selectedCount);
        cart.setSelectedAmount(selectedAmount);
        return cart;
    }

    @Override
    public void add(CartDTO dto) {
        Long userId = UserContext.getUserId();
        String key = userKey();
        // 校验商品
        Product p = productMapper.selectById(dto.getProductId());
        if (p == null || p.getStatus() == 0) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        if (dto.getQuantity() > stockService.getStock(p.getId())) {
            throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
        }
        // 已存在则累加
        CartItem existing = parseItem(redisTemplate.opsForHash().get(key, String.valueOf(p.getId())));
        int newQty = (existing == null ? 0 : existing.getQuantity()) + dto.getQuantity();
        CartItem item = new CartItem();
        item.setQuantity(newQty);
        item.setSelected(existing != null ? existing.getSelected() : true);
        redisTemplate.opsForHash().put(key, String.valueOf(p.getId()), item);
        // 记录加购行为(用于个性化推荐, 权重3)
        userBehaviorService.recordCart(p.getId(), p.getCategoryId());
        log.info("购物车加购: userId={}, productId={}, qty={}", userId, p.getId(), newQty);
    }

    @Override
    public void updateQuantity(CartDTO dto) {
        String key = userKey();
        Object raw = redisTemplate.opsForHash().get(key, String.valueOf(dto.getProductId()));
        CartItem item = parseItem(raw);
        if (item == null) {
            throw new BusinessException(ResultCode.CART_ITEM_NOT_FOUND);
        }
        item.setQuantity(dto.getQuantity());
        redisTemplate.opsForHash().put(key, String.valueOf(dto.getProductId()), item);
    }

    @Override
    public void toggleSelect(CartSelectDTO dto) {
        String key = userKey();
        Object raw = redisTemplate.opsForHash().get(key, String.valueOf(dto.getProductId()));
        CartItem item = parseItem(raw);
        if (item == null) {
            throw new BusinessException(ResultCode.CART_ITEM_NOT_FOUND);
        }
        item.setSelected(dto.getSelected());
        redisTemplate.opsForHash().put(key, String.valueOf(dto.getProductId()), item);
    }

    @Override
    public void toggleSelectAll(Boolean selected) {
        String key = userKey();
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        for (Map.Entry<Object, Object> e : entries.entrySet()) {
            CartItem item = parseItem(e.getValue());
            item.setSelected(selected);
            redisTemplate.opsForHash().put(key, e.getKey(), item);
        }
    }

    @Override
    public void remove(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) return;
        String key = userKey();
        Object[] fields = productIds.stream().map(String::valueOf).toArray();
        redisTemplate.opsForHash().delete(key, fields);
    }

    @Override
    public void merge(String deviceToken) {
        if (deviceToken == null || deviceToken.isBlank()) return;
        String tempKey = tempPrefix + deviceToken;
        Map<Object, Object> tempEntries = redisTemplate.opsForHash().entries(tempKey);
        if (tempEntries.isEmpty()) return;

        String userK = userKey();
        for (Map.Entry<Object, Object> e : tempEntries.entrySet()) {
            String pid = e.getKey().toString();
            CartItem tempItem = parseItem(e.getValue());
            CartItem userItem = parseItem(redisTemplate.opsForHash().get(userK, pid));
            if (userItem == null) {
                redisTemplate.opsForHash().put(userK, pid, tempItem);
            } else {
                // 同商品数量累加, 选中态取或
                userItem.setQuantity(userItem.getQuantity() + tempItem.getQuantity());
                userItem.setSelected(userItem.getSelected() || tempItem.getSelected());
                redisTemplate.opsForHash().put(userK, pid, userItem);
            }
        }
        // 合并后清空离线购物车
        redisTemplate.delete(tempKey);
        log.info("购物车合并完成: userId={}, device={}", UserContext.getUserId(), deviceToken);
    }

    @Override
    public void clearSelected() {
        String key = userKey();
        List<String> toRemove = getSelectedProductIds().stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
        if (!toRemove.isEmpty()) {
            redisTemplate.opsForHash().delete(key, toRemove.toArray());
        }
    }

    @Override
    public List<Long> getSelectedProductIds() {
        String key = userKey();
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        List<Long> ids = new ArrayList<>();
        for (Map.Entry<Object, Object> e : entries.entrySet()) {
            CartItem item = parseItem(e.getValue());
            if (item.getSelected()) {
                ids.add(Long.valueOf(e.getKey().toString()));
            }
        }
        return ids;
    }

    private String userKey() {
        return userPrefix + UserContext.getUserId();
    }

    /** 解析购物车项(支持 JSON 与字符串) */
    private CartItem parseItem(Object raw) {
        if (raw == null) return null;
        if (raw instanceof CartItem) return (CartItem) raw;
        // Jackson 反序列化后可能是 LinkedHashMap
        try {
            return objectMapper.convertValue(raw, CartItem.class);
        } catch (Exception e) {
            return null;
        }
    }

    /** 购物车项内部结构 */
    @lombok.Data
    public static class CartItem {
        private Integer quantity;
        private Boolean selected;
    }
}
