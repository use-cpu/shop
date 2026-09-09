-- ====================================================================
-- 电商购物商城系统 建表脚本
-- 数据库: shop_mall
-- 字符集: utf8mb4
-- 说明: 购物车数据存于 Redis, 无需建表; 行为表用于个性化推荐
-- ====================================================================

DROP DATABASE IF EXISTS shop_mall;
CREATE DATABASE shop_mall DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE shop_mall;

-- --------------------------------------------------------------------
-- 1. 用户表 (user)
-- --------------------------------------------------------------------
CREATE TABLE `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`    VARCHAR(50)  NOT NULL COMMENT '登录名',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    `nickname`    VARCHAR(50)           DEFAULT NULL COMMENT '昵称',
    `phone`       VARCHAR(20)           DEFAULT NULL COMMENT '手机号',
    `email`       VARCHAR(50)           DEFAULT NULL COMMENT '邮箱',
    `avatar`      VARCHAR(255)          DEFAULT NULL COMMENT '头像URL',
    `role`        TINYINT      NOT NULL DEFAULT 0 COMMENT '角色: 0普通用户 1管理员',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0禁用 1正常',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

-- --------------------------------------------------------------------
-- 2. 收货地址表 (address)
-- --------------------------------------------------------------------
CREATE TABLE `address` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`        BIGINT       NOT NULL COMMENT '用户ID',
    `receiver_name`  VARCHAR(50)  NOT NULL COMMENT '收货人',
    `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人电话',
    `province`       VARCHAR(50)           DEFAULT NULL COMMENT '省',
    `city`           VARCHAR(50)           DEFAULT NULL COMMENT '市',
    `district`       VARCHAR(50)           DEFAULT NULL COMMENT '区/县',
    `detail`         VARCHAR(255)          DEFAULT NULL COMMENT '详细地址',
    `is_default`     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否默认: 0否 1是',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '收货地址表';

-- --------------------------------------------------------------------
-- 3. 商品分类表 (category) —— 支持多级, parent_id=0 为一级
-- --------------------------------------------------------------------
CREATE TABLE `category` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`        VARCHAR(50)  NOT NULL COMMENT '分类名',
    `parent_id`   BIGINT       NOT NULL DEFAULT 0 COMMENT '父分类ID, 0表示一级分类',
    `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序(升序)',
    `icon`        VARCHAR(255)          DEFAULT NULL COMMENT '图标',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0禁用 1正常',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品分类表';

-- --------------------------------------------------------------------
-- 4. 商品表 (product)
--    version 字段用于 MyBatis-Plus 乐观锁, 防止并发超卖
-- --------------------------------------------------------------------
CREATE TABLE `product` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `category_id`    BIGINT        NOT NULL COMMENT '分类ID',
    `name`           VARCHAR(100)  NOT NULL COMMENT '商品名',
    `subtitle`       VARCHAR(200)           DEFAULT NULL COMMENT '副标题',
    `main_image`     VARCHAR(255)           DEFAULT NULL COMMENT '主图URL',
    `images`         VARCHAR(1000)          DEFAULT NULL COMMENT '多图URL, 逗号分隔',
    `detail`         TEXT                   DEFAULT NULL COMMENT '商品详情(富文本)',
    `price`          DECIMAL(10,2) NOT NULL COMMENT '售价',
    `original_price` DECIMAL(10,2)          DEFAULT NULL COMMENT '原价',
    `stock`          INT           NOT NULL DEFAULT 0 COMMENT '库存',
    `sales`          INT           NOT NULL DEFAULT 0 COMMENT '销量',
    `status`         TINYINT       NOT NULL DEFAULT 1 COMMENT '状态: 0下架 1上架',
    `version`        INT           NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status_sales` (`status`, `sales`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品表';

-- --------------------------------------------------------------------
-- 5. 订单主表 (order_info) —— 避开 MySQL 保留字 order
--    status: 0待支付 1已支付 2已发货 3已完成 4已关闭
-- --------------------------------------------------------------------
CREATE TABLE `order_info` (
    `id`               BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_no`         VARCHAR(32)   NOT NULL COMMENT '订单号',
    `user_id`          BIGINT        NOT NULL COMMENT '用户ID',
    `total_amount`     DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    `total_quantity`   INT           NOT NULL COMMENT '商品总数量',
    `status`           TINYINT       NOT NULL DEFAULT 0 COMMENT '订单状态: 0待支付1已支付2已发货3已完成4已关闭',
    `receiver_name`    VARCHAR(50)            DEFAULT NULL COMMENT '收货人(快照)',
    `receiver_phone`   VARCHAR(20)            DEFAULT NULL COMMENT '收货电话(快照)',
    `receiver_address` VARCHAR(500)           DEFAULT NULL COMMENT '收货地址(快照)',
    `pay_time`         DATETIME               DEFAULT NULL COMMENT '支付时间',
    `ship_time`        DATETIME               DEFAULT NULL COMMENT '发货时间',
    `finish_time`      DATETIME               DEFAULT NULL COMMENT '完成时间',
    `close_time`       DATETIME               DEFAULT NULL COMMENT '关闭时间',
    `create_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_status` (`user_id`, `status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单主表';

-- --------------------------------------------------------------------
-- 6. 订单明细表 (order_item)
-- --------------------------------------------------------------------
CREATE TABLE `order_item` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id`      BIGINT        NOT NULL COMMENT '订单ID',
    `order_no`      VARCHAR(32)   NOT NULL COMMENT '订单号(冗余便于查询)',
    `product_id`    BIGINT        NOT NULL COMMENT '商品ID',
    `product_name`  VARCHAR(100)  NOT NULL COMMENT '商品名(下单快照)',
    `product_image` VARCHAR(255)           DEFAULT NULL COMMENT '商品主图(快照)',
    `price`         DECIMAL(10,2) NOT NULL COMMENT '成交单价',
    `quantity`      INT           NOT NULL COMMENT '购买数量',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_order_no` (`order_no`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单明细表';

-- --------------------------------------------------------------------
-- 7. 用户行为表 (user_behavior) —— 个性化推荐数据源
--    behavior_type: 1浏览 2加购; weight: 浏览=1 加购=3
-- --------------------------------------------------------------------
CREATE TABLE `user_behavior` (
    `id`            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`       BIGINT   NOT NULL COMMENT '用户ID',
    `product_id`   BIGINT   NOT NULL COMMENT '商品ID',
    `category_id`  BIGINT   NOT NULL COMMENT '商品所属分类(冗余,加速推荐聚合)',
    `behavior_type` TINYINT NOT NULL COMMENT '行为类型: 1浏览 2加购',
    `weight`        INT      NOT NULL DEFAULT 1 COMMENT '行为权重: 浏览=1 加购=3',
    `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_product_behavior` (`user_id`, `product_id`, `behavior_type`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户行为表';

-- ====================================================================
-- 初始化数据: 默认管理员 + 一级分类 + 示例商品
-- ====================================================================

-- 默认管理员: 账号 admin / 密码 admin123 (BCrypt加密结果)
INSERT INTO `user` (`username`, `password`, `nickname`, `role`, `status`)
VALUES ('admin', '$2a$10$HW3xBniFlvrdsOgO/ZRl4urN6fqB6LcC1ocytfYk5Rbdjc8y5Dg5C', '超级管理员', 1, 1);

-- 一级分类
INSERT INTO `category` (`name`, `parent_id`, `sort`) VALUES
('数码', 0, 1),
('服饰', 0, 2),
('食品', 0, 3),
('家居', 0, 4);

-- 数码下的二级分类
INSERT INTO `category` (`name`, `parent_id`, `sort`) VALUES
('手机', 1, 1),
('电脑', 1, 2),
('配件', 1, 3);

-- 示例商品 (图片使用占位, 实际可替换)
INSERT INTO `product` (`category_id`, `name`, `subtitle`, `main_image`, `price`, `original_price`, `stock`, `sales`, `status`) VALUES
(5, '智能手机 X1', '6.5英寸全面屏 旗舰芯片', 'https://picsum.photos/seed/p1/600/600', 2999.00, 3499.00, 100, 256, 1),
(5, '智能手机 P30', '拍照神器 超长续航', 'https://picsum.photos/seed/p2/600/600', 1999.00, 2299.00, 80, 188, 1),
(6, '轻薄笔记本 Air', '13英寸 16GB+512GB', 'https://picsum.photos/seed/p3/600/600', 4999.00, 5499.00, 50, 120, 1),
(6, '游戏本 T90', 'RTX高性能显卡', 'https://picsum.photos/seed/p4/600/600', 7999.00, 8999.00, 30, 66, 1),
(7, '蓝牙耳机 Pro', '主动降噪 续航30小时', 'https://picsum.photos/seed/p5/600/600', 399.00, 499.00, 200, 520, 1),
(2, '纯棉T恤', '舒适透气 多色可选', 'https://picsum.photos/seed/p6/600/600', 79.00, 129.00, 300, 880, 1),
(3, '牛奶饼干礼盒', '营养早餐 整箱装', 'https://picsum.photos/seed/p7/600/600', 49.00, 69.00, 500, 1200, 1),
(4, '北欧极简台灯', '护眼LED 三档调光', 'https://picsum.photos/seed/p8/600/600', 129.00, 199.00, 150, 340, 1);
