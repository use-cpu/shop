package com.shop.mall.interceptor;

import com.shop.mall.common.BusinessException;
import com.shop.mall.common.ResultCode;
import com.shop.mall.utils.JwtUtils;
import com.shop.mall.utils.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 拦截器
 * 流程:
 *   1. 从请求头 Authorization 提取 "Bearer xxx" 形式的 token
 *   2. 解析并校验过期时间
 *   3. 写入 UserContext, 供后续业务使用
 *   4. afterCompletion 清理 ThreadLocal, 防止内存泄漏
 *
 * @author shop-mall
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Value("${shop.jwt.header}")
    private String headerName;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader(headerName);
        if (token == null || token.isBlank()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 兼容 "Bearer xxx" 与 "xxx" 两种形式
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Claims claims = jwtUtils.parseToken(token);
        if (claims == null || jwtUtils.isExpired(claims)) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 写入上下文
        UserContext.set(new UserContext.LoginUser(
                jwtUtils.getUserId(claims),
                jwtUtils.getUsername(claims),
                jwtUtils.getRole(claims)
        ));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                               Object handler, Exception ex) {
        UserContext.clear();
    }
}
