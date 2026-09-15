package com.shop.mall.config;

import com.shop.mall.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC 配置
 * 注册 JWT 拦截器, 拦截需登录的接口; 放行登录/注册/商品公开查询等。
 * 配置上传文件的静态资源映射, 使 /upload/** 可访问本地 uploads 目录。
 *
 * @author shop-mall
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Value("${shop.upload.path:./uploads/}")
    private String uploadPath;

    @Value("${shop.upload.url-prefix:/upload/}")
    private String urlPrefix;

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
                        "/upload/**",          // 上传的静态图片资源(浏览器直接访问)
                        "/error",
                        "/favicon.ico"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /upload/** 映射到本地 uploads 目录, 前端可直接通过 /upload/xxx.jpg 访问图片
        // 转为绝对路径, 避免 Tomcat 临时工作目录干扰
        String absPath = new File(uploadPath).getAbsolutePath() + File.separator;
        registry.addResourceHandler(urlPrefix + "**")
                .addResourceLocations("file:" + absPath);
    }
}
