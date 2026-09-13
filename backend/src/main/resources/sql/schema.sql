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
    `main_image`     VARCHAR(1000)          DEFAULT NULL COMMENT '主图URL',
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
    `product_image` VARCHAR(1000)          DEFAULT NULL COMMENT '商品主图(快照)',
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

-- 示例商品 (主图为 AI 生成的对应商品图)
INSERT INTO `product` (`category_id`, `name`, `subtitle`, `main_image`, `price`, `original_price`, `stock`, `sales`, `status`) VALUES
(5, '智能手机 X1', '6.5英寸全面屏 旗舰芯片', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=Premium%20flagship%20smartphone%20with%20large%20full%20screen%20display%2C%20sleek%20modern%20design%2C%20e-commerce%20product%20photography%20on%20clean%20white%20background%2C%20centered%2C%20high%20detail&image_size=square_hd', 2999.00, 3499.00, 100, 256, 1),
(5, '智能手机 P30', '拍照神器 超长续航', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=Modern%20smartphone%20with%20triple%20camera%20system%20on%20back%2C%20camera%20phone%20product%20photo%2C%20e-commerce%20photography%20on%20clean%20white%20background%2C%20centered%2C%20high%20detail&image_size=square_hd', 1999.00, 2299.00, 80, 188, 1),
(6, '轻薄笔记本 Air', '13英寸 16GB+512GB', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=Ultra%20slim%20lightweight%20silver%20laptop%2013%20inch%2C%20thin%20bezels%2C%20open%20notebook%2C%20e-commerce%20product%20photography%20on%20clean%20white%20background%2C%20centered&image_size=square_hd', 4999.00, 5499.00, 50, 120, 1),
(6, '游戏本 T90', 'RTX高性能显卡', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=High%20performance%20gaming%20laptop%20with%20RGB%20backlit%20keyboard%2C%20dark%20aggressive%20design%2C%20open%20notebook%2C%20e-commerce%20product%20photo%20on%20dark%20clean%20background%2C%20centered&image_size=square_hd', 7999.00, 8999.00, 30, 66, 1),
(7, '蓝牙耳机 Pro', '主动降噪 续航30小时', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=Wireless%20bluetooth%20earbuds%20with%20open%20charging%20case%2C%20white%20earphones%2C%20e-commerce%20product%20photography%20on%20clean%20white%20background%2C%20centered%2C%20high%20detail&image_size=square_hd', 399.00, 499.00, 200, 520, 1),
(2, '纯棉T恤', '舒适透气 多色可选', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=Folded%20premium%20cotton%20t-shirt%2C%20casual%20apparel%20clothing%2C%20neatly%20stacked%2C%20e-commerce%20product%20photo%20on%20clean%20white%20background%2C%20centered%2C%20high%20detail&image_size=square_hd', 79.00, 129.00, 300, 880, 1),
(3, '牛奶饼干礼盒', '营养早餐 整箱装', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=Milk%20cookies%20gift%20box%20packaging%20with%20biscuits%20and%20milk%2C%20food%20gift%20set%2C%20e-commerce%20product%20photography%20on%20clean%20white%20background%2C%20centered&image_size=square_hd', 49.00, 69.00, 500, 1200, 1),
(4, '北欧极简台灯', '护眼LED 三档调光', 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=Minimalist%20nordic%20style%20LED%20desk%20lamp%2C%20simple%20scandinavian%20design%2C%20warm%20light%2C%20eye-care%20table%20light%2C%20e-commerce%20product%20photo%20on%20clean%20white%20background%2C%20centered&image_size=square_hd', 129.00, 199.00, 150, 340, 1);

-- 商品详情描述
UPDATE product SET detail = '<h3>智能手机 X1</h3><p>6.5 英寸全面屏旗舰手机, 搭载高性能芯片与 AI 影像系统, 无论是游戏、影像还是日常办公都能流畅应对。</p><h4>核心亮点</h4><ul><li>6.5 英寸 AMOLED 全面屏, 120Hz 高刷新率</li><li>旗舰级处理器, 性能强劲功耗更低</li><li>5000mAh 大电池 + 67W 快充</li><li>4800 万像素 AI 三摄影像系统</li></ul><h4>服务保障</h4><p>全国联保一年 · 7 天无理由退货 · 顺丰包邮</p>' WHERE name = '智能手机 X1';
UPDATE product SET detail = '<h3>智能手机 P30</h3><p>专为影像爱好者打造, 超感光三摄系统配合夜景算法, 白天黑夜都能拍出大片质感, 续航更是一天一充无压力。</p><h4>核心亮点</h4><ul><li>5000 万像素超感光主摄 + 双副摄</li><li>专业夜景模式, 暗光清晰成像</li><li>4500mAh 电池, 超长续航</li><li>轻薄机身, 单手握持舒适</li></ul><h4>服务保障</h4><p>全国联保一年 · 7 天无理由退货 · 顺丰包邮</p>' WHERE name = '智能手机 P30';
UPDATE product SET detail = '<h3>轻薄笔记本 Air</h3><p>13 英寸轻薄机身仅重 1.2kg, 16GB+512GB 黄金配置, 全金属一体成型工艺, 移动办公与学习的理想之选。</p><h4>核心亮点</h4><ul><li>13 英寸 2K 高色域屏幕</li><li>16GB 大内存 + 512GB 固态硬盘</li><li>全金属机身仅 1.2kg, 厚度 14.9mm</li><li>长续航 18 小时, 支持快充</li></ul><h4>服务保障</h4><p>全国联保两年 · 7 天无理由退货 · 顺丰包邮</p>' WHERE name = '轻薄笔记本 Air';
UPDATE product SET detail = '<h3>游戏本 T90</h3><p>搭载 RTX 高性能独立显卡与高刷新率电竞屏, 竞级散热系统压制核心温度, 大型 3A 游戏高画质流畅运行。</p><h4>核心亮点</h4><ul><li>RTX 高性能显卡, 光线追踪加持</li><li>15.6 英寸 165Hz 电竞高刷屏</li><li>四出风口双风扇竞级散热</li><li>RGB 背光键盘, 丰富灯效</li></ul><h4>服务保障</h4><p>全国联保两年 · 7 天无理由退货 · 顺丰包邮</p>' WHERE name = '游戏本 T90';
UPDATE product SET detail = '<h3>蓝牙耳机 Pro</h3><p>主动降噪蓝牙耳机, 最高 30 小时超长续航, 人声通透模式与低延迟游戏模式, 通勤运动全场景适用。</p><h4>核心亮点</h4><ul><li>主动降噪, 智能通透模式</li><li>单次 8 小时 + 充电盒共 30 小时续航</li><li>蓝牙 5.3 低延迟连接</li><li>IPX5 防水, 运动无忧</li></ul><h4>服务保障</h4><p>全国联保一年 · 7 天无理由退货 · 顺丰包邮</p>' WHERE name = '蓝牙耳机 Pro';
UPDATE product SET detail = '<h3>纯棉 T 恤</h3><p>精选 100% 新疆长绒棉, 亲肤透气不起球, 多色多码可选, 是春夏百搭的必备基础款。</p><h4>核心亮点</h4><ul><li>100% 纯棉面料, 亲肤透气</li><li>精纱工艺, 不易起球变形</li><li>多色可选: 白/灰/黑等</li><li>基础百搭, 男女同款</li></ul><h4>服务保障</h4><p>7 天无理由退换 · 赠品运费险 · 极速发货</p>' WHERE name = '纯棉T恤';
UPDATE product SET detail = '<h3>牛奶饼干礼盒</h3><p>精选小麦与新西兰乳粉, 香酥可口奶香浓郁, 精美礼盒装, 早餐下午茶、送礼自用两相宜。</p><h4>核心亮点</h4><ul><li>新西兰进口乳粉, 奶香浓郁</li><li>独立小包装, 新鲜便携</li><li>整箱 1kg 装超实惠</li><li>精美礼盒, 送礼有面子</li></ul><h4>服务保障</h4><p>保质期 12 个月 · 破损包赔 · 极速发货</p>' WHERE name = '牛奶饼干礼盒';
UPDATE product SET detail = '<h3>北欧极简台灯</h3><p>北欧简约设计, 护眼 LED 光源三档调光, 无频闪无蓝光危害, 学习办公阅读的理想照明伙伴。</p><h4>核心亮点</h4><ul><li>护眼 LED, 无频闪低蓝光</li><li>三档调光, 触摸开关</li><li>360° 柔光罩, 光线均匀不刺眼</li><li>北欧极简造型, 百搭家居风格</li></ul><h4>服务保障</h4><p>全国联保一年 · 7 天无理由退货 · 极速发货</p>' WHERE name = '北欧极简台灯';
