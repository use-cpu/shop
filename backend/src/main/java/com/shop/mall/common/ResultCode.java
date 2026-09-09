package com.shop.mall.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 系统统一状态码枚举
 * 约定: 0=成功; 1xxxx=通用错误; 2xxxx=用户模块; 3xxxx=商品模块;
 *      4xxxx=购物车; 5xxxx=订单; 6xxxx=管理后台; 9xxxx=系统异常
 *
 * @author shop-mall
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /* ---------------- 成功 ---------------- */
    SUCCESS(0, "成功"),

    /* ---------------- 通用错误 1xxxx ---------------- */
    FAIL(10000, "失败"),
    PARAM_INVALID(10001, "参数校验失败"),
    UNAUTHORIZED(10002, "未登录或登录已过期"),
    FORBIDDEN(10003, "无权限访问"),
    RESOURCE_NOT_FOUND(10004, "资源不存在"),
    TOO_MANY_REQUESTS(10005, "请求过于频繁"),

    /* ---------------- 用户模块 2xxxx ---------------- */
    USER_EXIST(20001, "用户名已存在"),
    USER_NOT_FOUND(20002, "用户不存在"),
    PASSWORD_ERROR(20003, "密码错误"),
    USER_DISABLED(20004, "账号已被禁用"),
    OLD_PASSWORD_ERROR(20005, "原密码错误"),

    /* ---------------- 商品模块 3xxxx ---------------- */
    PRODUCT_NOT_FOUND(30001, "商品不存在"),
    PRODUCT_OFF_SHELF(30002, "商品已下架"),
    STOCK_NOT_ENOUGH(30003, "库存不足"),
    CATEGORY_NOT_FOUND(30004, "分类不存在"),

    /* ---------------- 购物车 4xxxx ---------------- */
    CART_ITEM_NOT_FOUND(40001, "购物车商品不存在"),

    /* ---------------- 订单 5xxxx ---------------- */
    ORDER_NOT_FOUND(50001, "订单不存在"),
    ORDER_STATUS_INVALID(50002, "订单状态不允许此操作"),
    ORDER_PAY_TIMEOUT(50003, "订单已超时关闭"),
    ORDER_EMPTY_CART(50004, "购物车为空, 无法下单"),

    /* ---------------- 管理后台 6xxxx ---------------- */
    ADMIN_REQUIRED(60001, "需要管理员权限"),

    /* ---------------- 系统异常 9xxxx ---------------- */
    SYSTEM_ERROR(90000, "系统异常");

    private final Integer code;
    private final String  message;
}
