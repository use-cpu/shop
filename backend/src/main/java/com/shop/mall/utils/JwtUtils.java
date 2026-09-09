package com.shop.mall.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 职责: 生成 / 解析 / 校验 token; 载荷中存放 userId, username, role。
 * 密钥与过期时间从 application.yml 读取(shop.jwt.*)。
 *
 * @author shop-mall
 */
@Slf4j
@Component
public class JwtUtils {

    @Value("${shop.jwt.secret}")
    private String secret;

    @Value("${shop.jwt.expiration}")
    private Long expiration;

    private SecretKey key;

    @PostConstruct
    public void init() {
        // 密钥不足 32 字节则补齐, 满足 HS256 要求
        byte[] keyBytes = (secret == null ? "shop-mall-secret" : secret)
                .getBytes(StandardCharsets.UTF_8);
        int len = Math.max(keyBytes.length, 32);
        byte[] padded = new byte[len];
        System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
        this.key = Keys.hmacShaKeyFor(padded);
        log.info("JWT 工具初始化完成, 过期时间={}ms", expiration);
    }

    /**
     * 生成 token
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param role     角色 0用户 1管理员
     */
    public String generateToken(Long userId, String username, Integer role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);
        Date now = new Date();
        Date exp = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    /** 解析 token, 返回 Claims; 失败返回 null */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.warn("JWT 解析失败: {}", e.getMessage());
            return null;
        }
    }

    /** 是否已过期 */
    public boolean isExpired(Claims claims) {
        return claims == null || claims.getExpiration().before(new Date());
    }

    public Long getUserId(Claims claims) {
        Object v = claims.get("userId");
        if (v instanceof Integer) return ((Integer) v).longValue();
        if (v instanceof Long)    return (Long) v;
        return Long.valueOf(v.toString());
    }

    public String getUsername(Claims claims) {
        return claims.get("username", String.class);
    }

    public Integer getRole(Claims claims) {
        Object v = claims.get("role");
        return v == null ? 0 : Integer.valueOf(v.toString());
    }
}
