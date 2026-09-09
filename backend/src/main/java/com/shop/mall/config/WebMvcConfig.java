package com.shop.mall.config;

import com.shop.mall.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 注册 JWT 拦截器, 拦截需登录的接口; 放行登录/注册/商品公开查询等。
 *
 * @author shop-mall
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                // 公开接口白名单
                .excludePathPatterns(
                        "/auth/login",
                        "/auth/register",
                        "/auth/admin/login",
                        "/product/list",
                        "/product/hot",
                        "/product/detail/**",
                        "/category/tree",
                        "/recommend",
                        "/error",
                        "/favicon.ico"
                );
    }
}
