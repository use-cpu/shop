package com.shop.mall.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.mall.common.BusinessException;
import com.shop.mall.common.ResultCode;
import com.shop.mall.dto.BuyNowDTO;
import com.shop.mall.dto.CheckoutDTO;
import com.shop.mall.entity.Address;
import com.shop.mall.entity.OrderInfo;
import com.shop.mall.entity.OrderItem;
import com.shop.mall.entity.Product;
import com.shop.mall.mapper.OrderInfoMapper;
import com.shop.mall.mapper.OrderItemMapper;
import com.shop.mall.mapper.ProductMapper;
import com.shop.mall.service.AddressService;
import com.shop.mall.service.CartService;
import com.shop.mall.service.OrderService;
import com.shop.mall.service.StockService;
import com.shop.mall.service.UserBehaviorService;
import com.shop.mall.utils.UserContext;
import com.shop.mall.vo.CartItemVO;
import com.shop.mall.vo.CartVO;
import com.shop.mall.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单服务实现
 *
 * 状态机: 0待支付 → 1已支付 → 2已发货 → 3已完成; 30min 未支付 → 4已关闭
 *
 * @author shop-mall
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired private OrderInfoMapper orderInfoMapper;
    @Autowired private OrderItemMapper orderItemMapper;
    @Autowired private ProductMapper productMapper;
    @Autowired private AddressService addressService;
    @Autowired private CartService cartService;
    @Autowired private StockService stockService;
    @Autowired private UserBehaviorService userBehaviorService;

    @Value("${shop.order.pay-timeout}")
    private Integer payTimeoutMinutes;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderInfo createOrder(CheckoutDTO dto) {
        Long userId = UserContext.getUserId();

        // 1. 校验收货地址
        Address addr = addressService.getDefault(userId);
        if (addr == null) {
            // 取用户最新地址
            addr = addressService.listMine().stream().findFirst().orElse(null);
        }
        if (addr == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "未找到收货地址");
        }

        // 2. 获取选中项
        CartVO cart = cartService.list();
        List<CartItemVO> selected = cart.getItems().stream()
                .filter(CartItemVO::getSelected)
                .collect(Collectors.toList());
        if (selected.isEmpty()) {
            throw new BusinessException(ResultCode.ORDER_EMPTY_CART);
        }

        // 3. 扣减库存(Redis原子 + DB乐观锁); 失败则回滚已扣减的
        List<Long> deducted = new ArrayList<>();
        for (CartItemVO item : selected) {
            boolean ok = stockService.deduct(item.getProductId(), item.getQuantity());
            if (!ok) {
                // 回滚已扣减库存
                for (Long pid : deducted) {
                    int qty = selected.stream()
                            .filter(i -> i.getProductId().equals(pid))
                            .findFirst()
                            .map(CartItemVO::getQuantity)
                            .orElse(0);
                    stockService.rollback(pid, qty);
                }
                throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH,
                        "商品 [" + item.getProductName() + "] 库存不足");
            }
            deducted.add(item.getProductId());
        }

        // 4. 构建订单
        OrderInfo order = new OrderInfo();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setStatus(0);
        order.setReceiverName(addr.getReceiverName());
        order.setReceiverPhone(addr.getReceiverPhone());
        order.setReceiverAddress(joinAddress(addr));
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalQty = 0;
        List<OrderItem> items = new ArrayList<>();
        for (CartItemVO item : selected) {
            Product p = productMapper.selectById(item.getProductId());
            BigDecimal subtotal = p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
            totalQty += item.getQuantity();

            OrderItem oi = new OrderItem();
            oi.setOrderNo(order.getOrderNo());
            oi.setProductId(p.getId());
            oi.setProductName(p.getName());
            oi.setProductImage(p.getMainImage());
            oi.setPrice(p.getPrice());
            oi.setQuantity(item.getQuantity());
            items.add(oi);
        }
        order.setTotalAmount(totalAmount);
        order.setTotalQuantity(totalQty);
        orderInfoMapper.insert(order);

        // 5. 写入订单明细(需设置 order_id)
        for (OrderItem oi : items) {
            oi.setOrderId(order.getId());
            orderItemMapper.insert(oi);
        }

        // 6. 清空购物车选中项
        cartService.clearSelected();

        log.info("订单创建成功: orderNo={}, amount={}, qty={}", order.getOrderNo(), totalAmount, totalQty);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderInfo buyNow(BuyNowDTO dto) {
        Long userId = UserContext.getUserId();

        // 1. 校验收货地址
        Address addr = addressService.getDefault(userId);
        if (addr == null) {
            addr = addressService.listMine().stream().findFirst().orElse(null);
        }
        if (addr == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND, "未找到收货地址");
        }

        // 2. 校验商品
        Product p = productMapper.selectById(dto.getProductId());
        if (p == null || p.getStatus() == 0) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }

        // 3. 扣减库存(Redis 原子 + DB 乐观锁)
        boolean ok = stockService.deduct(p.getId(), dto.getQuantity());
        if (!ok) {
            throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH, "商品 [" + p.getName() + "] 库存不足");
        }

        // 4. 构建订单
        OrderInfo order = new OrderInfo();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setStatus(0);
        order.setReceiverName(addr.getReceiverName());
        order.setReceiverPhone(addr.getReceiverPhone());
        order.setReceiverAddress(joinAddress(addr));
        BigDecimal totalAmount = p.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));
        order.setTotalAmount(totalAmount);
        order.setTotalQuantity(dto.getQuantity());
        orderInfoMapper.insert(order);

        // 5. 订单明细
        OrderItem oi = new OrderItem();
        oi.setOrderId(order.getId());
        oi.setOrderNo(order.getOrderNo());
        oi.setProductId(p.getId());
        oi.setProductName(p.getName());
        oi.setProductImage(p.getMainImage());
        oi.setPrice(p.getPrice());
        oi.setQuantity(dto.getQuantity());
        orderItemMapper.insert(oi);

        // 6. 记录加购行为(用于推荐): buyNow 是强购买信号, 等同加购行为
        userBehaviorService.recordCart(p.getId(), p.getCategoryId());

        log.info("立即购买订单创建成功: orderNo={}, amount={}", order.getOrderNo(), totalAmount);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(String orderNo) {
        OrderInfo order = loadOrder(orderNo);
        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID, "订单状态不允许支付");
        }
        // 模拟支付回调: 直接将状态改为已支付
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        orderInfoMapper.updateById(order);
        log.info("订单支付成功(模拟回调): orderNo={}", orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(String orderNo) {
        OrderInfo order = loadOrder(orderNo);
        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID, "仅待支付订单可取消");
        }
        closeOrderAndRollbackStock(order);
        log.info("订单已取消: orderNo={}", orderNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(String orderNo) {
        OrderInfo order = loadOrder(orderNo);
        if (order.getStatus() != 2) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID, "仅已发货订单可确认收货");
        }
        order.setStatus(3);
        order.setFinishTime(LocalDateTime.now());
        orderInfoMapper.updateById(order);
        log.info("订单确认收货完成: orderNo={}", orderNo);
    }

    @Override
    public OrderVO detail(String orderNo) {
        OrderInfo order = loadOrder(orderNo);
        return toVO(order);
    }

    @Override
    public IPage<OrderVO> myOrders(Integer status, int pageNum, int pageSize) {
        Long userId = UserContext.getUserId();
        return queryPage(userId, status, pageNum, pageSize);
    }

    @Override
    public IPage<OrderVO> allOrders(Integer status, int pageNum, int pageSize) {
        UserContext.requireAdmin();
        return queryPage(null, status, pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void ship(String orderNo) {
        UserContext.requireAdmin();
        OrderInfo order = loadOrder(orderNo);
        if (order.getStatus() != 1) {
            throw new BusinessException(ResultCode.ORDER_STATUS_INVALID, "仅已支付订单可发货");
        }
        order.setStatus(2);
        order.setShipTime(LocalDateTime.now());
        orderInfoMapper.updateById(order);
        log.info("订单已发货: orderNo={}", orderNo);
    }

    /**
     * 定时任务: 每分钟扫描并关闭超时未支付订单
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeTimeoutOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(payTimeoutMinutes);
        List<OrderInfo> timeouts = orderInfoMapper.selectList(
                new LambdaQueryWrapper<OrderInfo>()
                        .eq(OrderInfo::getStatus, 0)
                        .lt(OrderInfo::getCreateTime, deadline));
        if (timeouts.isEmpty()) return;
        log.info("扫描到 {} 笔超时未支付订单, 执行关单", timeouts.size());
        for (OrderInfo order : timeouts) {
            closeOrderAndRollbackStock(order);
        }
    }

    /** 关单并回补库存 */
    private void closeOrderAndRollbackStock(OrderInfo order) {
        order.setStatus(4);
        order.setCloseTime(LocalDateTime.now());
        orderInfoMapper.updateById(order);
        // 回补库存
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderNo, order.getOrderNo()));
        for (OrderItem item : items) {
            stockService.rollback(item.getProductId(), item.getQuantity());
        }
    }

    private IPage<OrderVO> queryPage(Long userId, Integer status, int pageNum, int pageSize) {
        Page<OrderInfo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OrderInfo> w = new LambdaQueryWrapper<>();
        if (userId != null) w.eq(OrderInfo::getUserId, userId);
        if (status != null) w.eq(OrderInfo::getStatus, status);
        w.orderByDesc(OrderInfo::getCreateTime);
        IPage<OrderInfo> p = orderInfoMapper.selectPage(page, w);
        return p.convert(this::toVO);
    }

    private OrderInfo loadOrder(String orderNo) {
        OrderInfo order = orderInfoMapper.selectOne(
                new LambdaQueryWrapper<OrderInfo>().eq(OrderInfo::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        // 用户端校验归属: 普通用户只能查看自己的订单
        // 接口被 JWT 拦截器保护, 此处必有用户上下文
        Long userId = UserContext.getUserIdNullable();
        Integer role = UserContext.getRole();
        if (role != null && role == 0
                && !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return order;
    }

    /** 订单号: 时间戳 + 短ID */
    private String generateOrderNo() {
        return System.currentTimeMillis() + IdUtil.fastSimpleUUID().substring(0, 8).toUpperCase();
    }

    private String joinAddress(Address addr) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(addr.getProvince())) sb.append(addr.getProvince());
        if (StringUtils.hasText(addr.getCity()))     sb.append(addr.getCity());
        if (StringUtils.hasText(addr.getDistrict())) sb.append(addr.getDistrict());
        if (StringUtils.hasText(addr.getDetail()))  sb.append(addr.getDetail());
        return sb.toString();
    }

    private OrderVO toVO(OrderInfo order) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setTotalQuantity(order.getTotalQuantity());
        vo.setStatus(order.getStatus());
        vo.setStatusText(statusText(order.getStatus()));
        vo.setReceiverName(order.getReceiverName());
        vo.setReceiverPhone(order.getReceiverPhone());
        vo.setReceiverAddress(order.getReceiverAddress());
        vo.setPayTime(order.getPayTime());
        vo.setShipTime(order.getShipTime());
        vo.setFinishTime(order.getFinishTime());
        vo.setCloseTime(order.getCloseTime());
        vo.setCreateTime(order.getCreateTime());
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderNo, order.getOrderNo()));
        vo.setOrderItems(items);
        return vo;
    }

    private String statusText(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "待支付";
            case 1: return "已支付";
            case 2: return "已发货";
            case 3: return "已完成";
            case 4: return "已关闭";
            default: return "未知";
        }
    }
}
