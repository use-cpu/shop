# ====================================================================
# 前端 Dockerfile: 多阶段构建
# 阶段1: Node 构建静态资源; 阶段2: Nginx 托管 + 反向代理后端
# ====================================================================

# ---------- 阶段1: 构建 ----------
FROM node:20-alpine AS builder
WORKDIR /build
# 利用缓存加速依赖安装
COPY package.json ./
RUN npm config set registry https://registry.npmmirror.com && npm install
COPY . .
RUN npm run build

# ---------- 阶段2: Nginx 运行 ----------
FROM nginx:1.25-alpine
# 清空默认配置, 避免多 server 块冲突
RUN rm -rf /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/default.conf
# 拷贝构建产物
COPY --from=builder /build/dist /usr/share/nginx/html

EXPOSE 80

# 启动前用环境变量 BACKEND_HOST 替换配置占位符, 默认 backend:8080
# 前台运行 Nginx, 防止容器退出
CMD sed -i "s|__BACKEND_HOST__|${BACKEND_HOST:-backend:8080}|g" /etc/nginx/conf.d/default.conf && nginx -g "daemon off;"
