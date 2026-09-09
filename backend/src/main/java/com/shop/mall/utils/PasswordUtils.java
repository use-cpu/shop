package com.shop.mall.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具类
 * 使用 BCrypt 单向加密; 同一明文每次加密结果不同, 安全性高。
 * 注意: 未引入 spring-security 全量依赖, 仅使用其 bcript 实现。
 * 引入方式: spring-boot-starter-web 已传递 spring-security-crypto。
 *
 * @author shop-mall
 */
public class PasswordUtils {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /** 加密明文密码 */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /** 校验明文与密文是否匹配 */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) return false;
        return ENCODER.matches(rawPassword, encodedPassword);
    }
}
