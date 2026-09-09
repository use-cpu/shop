# ====================================================================
# 后端 Dockerfile: 多阶段构建
# 阶段1: Maven 编译打包; 阶段2: JRE 运行, 减小镜像体积
# ====================================================================

# ---------- 阶段1: 构建 ----------
FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /build
# 先拷贝 pom 加速依赖缓存
COPY pom.xml .
RUN mvn dependency:go-offline -B
# 拷贝源码并打包(跳过测试)
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---------- 阶段2: 运行 ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# 拷贝构建产物
COPY --from=builder /build/target/shop-mall.jar app.jar

# 暴露 Spring Boot 端口
EXPOSE 8080

# 启动命令; 通过环境变量注入数据库/Redis/JWT 配置
ENTRYPOINT ["java", "-jar", "app.jar"]
