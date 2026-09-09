# 电商购物商城系统

> 毕设题目：电商场景下 Java 网上购物商城系统个性化设计

一个完整的前后端分离电商系统，包含用户端与管理后台。核心实现个性化推荐、Redis 购物车、订单状态机、库存防超卖（Redis+乐观锁）与自动关单。

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Spring Boot 3.2 + MyBatis-Plus 3.5 + MySQL 8 + Redis 7 |
| 前端 | Vue 3 + Vite 5 + Element Plus + Pinia + Vue Router |
| 鉴权 | JWT（自定义拦截器） |
| 部署 | Docker 多阶段构建 → GHCR 镜像 → Sealos |

## 目录结构

```
shop/
├── backend/                    # 后端 Spring Boot 工程
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/
│       ├── java/com/shop/mall/
│       │   ├── ShopMallApplication.java
│       │   ├── common/         # 统一响应/异常/状态码
│       │   ├── config/         # Redis/MyBatisPlus/Cors/WebMvc
│       │   ├── interceptor/    # JWT 拦截器
│       │   ├── utils/          # JwtUtils/RedisUtils/PasswordUtils/UserContext
│       │   ├── entity/         # 7 张表对应实体
│       │   ├── mapper/         # MyBatis-Plus Mapper
│       │   ├── service/       # 服务接口 + impl 实现
│       │   ├── controller/     # REST 控制器
│       │   ├── dto/           # 请求参数对象
│       │   └── vo/            # 返回视图对象
│       └── resources/
│           ├── application.yml
│           └── sql/schema.sql  # 建表 SQL（含初始数据）
├── frontend/                   # 前端 Vue3 工程
│   ├── package.json / vite.config.js
│   ├── Dockerfile / nginx.conf
│   └── src/
│       ├── api/               # 8 个 API 模块
│       ├── router/            # 路由 + 守卫
│       ├── stores/            # Pinia 用户状态
│       ├── views/             # 11 个用户端页 + 6 个后台页
│       └── components/        # Header / ProductCard
├── .github/workflows/         # GitHub Actions 构建/推送 GHCR
├── docker-compose.yml         # 本地一键编排
└── README.md
```

## 核心功能

1. **用户端**：注册登录、收货地址管理（CRUD + 默认地址）
2. **商品模块**：分类树、关键词搜索、商品详情；**个性化推荐**——基于用户浏览/加购行为的偏好分类推荐，新用户回退热销榜
3. **购物车**：Redis Hash 存储，增改数量、勾选结算，登录自动合并离线购物车
4. **订单模块**：状态机（待支付→已支付→已发货→已完成）；30 分钟未支付自动关单并释放库存；模拟支付回调
5. **库存防超卖**：Redis 原子 DECR + DB 乐观锁（@Version）双重保障
6. **管理后台**：商品上下架/库存维护、订单管理/发货、用户管理

## 数据库设计

共 7 张表（购物车存 Redis 无需建表）：

| 表 | 说明 |
|---|---|
| user | 用户（0普通/1管理员） |
| address | 收货地址 |
| category | 商品分类（支持多级） |
| product | 商品（含 version 乐观锁字段） |
| order_info | 订单主表（状态机 0-4） |
| order_item | 订单明细（含下单快照） |
| user_behavior | 用户行为（推荐数据源） |

完整建表脚本见 `backend/src/main/resources/sql/schema.sql`，含字段注释与初始数据（默认管理员 admin/admin123 + 示例商品）。

## 本地启动

### 环境要求
- JDK 17+、Maven 3.9+、Node.js 20+、MySQL 8、Redis 7

### 方式一：分别启动（开发推荐）

1. 初始化数据库
   ```bash
   mysql -uroot -p < backend/src/main/resources/sql/schema.sql
   ```

2. 启动后端（默认 8080）
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   或修改 `application.yml` 中数据库连接后运行。

3. 启动前端（默认 5173，已配置 /api 代理到 8080）
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

4. 访问 http://localhost:5173

### 方式二：Docker Compose 一键启动

```bash
docker compose up -d
```
- 前端：http://localhost
- 后端 API：http://localhost:8080/api
- MySQL 自动初始化（容器启动时执行 schema.sql）

## 镜像构建与 GHCR 推送

镜像通过 GitHub Actions 自动构建并推送至 GHCR（GitHub Container Registry），无需手动操作。

### 自动构建（推荐）

1. 将本仓库推送到 GitHub main 分支
2. GitHub Actions 自动触发，分别构建：
   - `ghcr.io/<owner>/shop-backend:latest`
   - `ghcr.io/<owner>/shop-frontend:latest`
3. 工作流末尾自动将 package 设为 public，便于 Sealos 拉取

### 手动构建

```bash
# 后端
cd backend
docker build -t shop-backend .

# 前端
cd frontend
docker build -t shop-frontend .
```

## Sealos 部署

### 1. 部署 MySQL

- 镜像：`mysql:8.0`
- 端口：3306
- 环境变量：`MYSQL_ROOT_PASSWORD=root`、`MYSQL_DATABASE=shop_mall`
- 初始化：部署后执行 `schema.sql` 建表（或通过 Sealos 终端导入）

### 2. 部署 Redis

- 镜像：`redis:7-alpine`
- 端口：6379

### 3. 部署后端

- 镜像：`ghcr.io/<owner>/shop-backend:latest`
- 端口：8080
- 公网访问：关闭（仅集群内部访问）
- 环境变量：
  ```
  DB_HOST=<sealos生成的mysql服务名>
  DB_PORT=3306
  DB_NAME=shop_mall
  DB_USER=root
  DB_PASSWORD=root
  REDIS_HOST=<sealos生成的redis服务名>
  REDIS_PORT=6379
  JWT_SECRET=ZmQ0ZGI5ZjJjYzQwN2Q0YzQwYzRkYzQwYzQwYzQw
  ```

### 4. 部署前端

- 镜像：`ghcr.io/<owner>/shop-frontend:latest`
- 端口：80
- 公网访问：开启
- 环境变量：`BACKEND_HOST=<sealos生成的后端服务名>:8080`

> **注意**：Sealos 自动生成的服务名带随机后缀，前端 `BACKEND_HOST` 必须填写后端实际生成的完整服务名，否则 Nginx 反向代理无法解析。

## 默认账号

| 角色 | 账号 | 密码 |
|---|---|---|
| 管理员 | admin | admin123 |
| 普通用户 | 注册获取 | 注册时设置 |

## 关键设计说明

### 个性化推荐
1. 用户浏览商品详情 / 加入购物车时，写入 `user_behavior`（浏览权重 1，加购权重 3）
2. 首页推荐时，聚合用户各分类权重之和，取 Top3 偏好分类
3. 从偏好分类查询用户近期未浏览的在售商品，按销量降序返回
4. 新用户无行为数据 → 回退到全平台热销榜

### 库存防超卖
- 第一层：Redis `DECR` 原子操作快速判定库存是否足够（挡住绝大多数并发请求）
- 第二层：DB `update set stock=stock-qty, version=version+1 where version=? and stock>=qty`（乐观锁兜底，保证最终一致性）
- 失败回滚：任一层失败均回滚已扣减的库存

### 订单状态机
```
待支付(0) ──支付──▶ 已支付(1) ──发货──▶ 已发货(2) ──确认收货──▶ 已完成(3)
   │
   └── 30min未支付 / 用户取消 ──▶ 已关闭(4)（释放库存）
```
定时任务每分钟扫描超时订单并自动关单。

## API 概览

| 模块 | 主要接口 |
|---|---|
| 鉴权 | `POST /api/auth/register` `POST /api/auth/login` `POST /api/auth/admin/login` `GET /api/auth/info` |
| 地址 | `GET/POST/PUT/DELETE /api/address` `PUT /api/address/default/{id}` |
| 分类 | `GET /api/category/tree` |
| 商品 | `GET /api/product/list` `GET /api/product/{id}` `GET /api/product/hot` |
| 推荐 | `GET /api/recommend` |
| 购物车 | `GET/POST/PUT/DELETE /api/cart` `PUT /api/cart/select` `POST /api/cart/merge` |
| 订单 | `POST /api/order` `GET /api/order/list` `GET /api/order/{orderNo}` `POST /api/order/pay/{orderNo}` |
| 管理 | `/api/product/*`（增删改查/上下架/库存） `/api/order/admin/*` `/api/admin/user/*` |
