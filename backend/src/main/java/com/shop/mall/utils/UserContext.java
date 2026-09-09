package com.shop.mall.utils;

import com.shop.mall.common.BusinessException;
import com.shop.mall.common.ResultCode;

/**
 * 当前登录用户上下文(ThreadLocal)
 * JWT 拦截器解析 token 后, 将用户信息写入 ThreadLocal,
 * Controller/Service 可直接通过 UserContext 获取当前用户。
 *
 * @author shop-mall
 */
public class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    /** 获取当前用户ID, 未登录抛异常 */
    public static Long getUserId() {
        LoginUser u = HOLDER.get();
        if (u == null) throw new BusinessException(ResultCode.UNAUTHORIZED);
        return u.getUserId();
    }

    /** 获取当前用户ID(允许未登录, 返回 null) */
    public static Long getUserIdNullable() {
        LoginUser u = HOLDER.get();
        return u == null ? null : u.getUserId();
    }

    public static Integer getRole() {
        LoginUser u = HOLDER.get();
        if (u == null) throw new BusinessException(ResultCode.UNAUTHORIZED);
        return u.getRole();
    }

    /** 要求管理员权限 */
    public static void requireAdmin() {
        Integer role = getRole();
        if (role == null || role != 1) {
            throw new BusinessException(ResultCode.ADMIN_REQUIRED);
        }
    }

    public static void clear() {
        HOLDER.remove();
    }

    /** 登录用户信息载体 */
    public static class LoginUser {
        private final Long userId;
        private final String username;
        private final Integer role;

        public LoginUser(Long userId, String username, Integer role) {
            this.userId = userId;
            this.username = username;
            this.role = role;
        }
        public Long getUserId() { return userId; }
        public String getUsername() { return username; }
        public Integer getRole() { return role; }
    }
}
