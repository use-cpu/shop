package com.shop.mall.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应封装
 * 结构: { "code": 0, "message": "成功", "data": {...} }
 * 前端通过 code===0 判定成功, 其余均为业务异常。
 *
 * @author shop-mall
 */
@Data
public class Result<T> implements Serializable {

    private Integer code;
    private String  message;
    private T       data;

    private Result() {}

    /** 成功, 无数据 */
    public static <T> Result<T> success() {
        Result<T> r = new Result<>();
        r.setCode(ResultCode.SUCCESS.getCode());
        r.setMessage(ResultCode.SUCCESS.getMessage());
        return r;
    }

    /** 成功, 带数据 */
    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(ResultCode.SUCCESS.getCode());
        r.setMessage(ResultCode.SUCCESS.getMessage());
        r.setData(data);
        return r;
    }

    /** 失败, 自定义状态码 */
    public static <T> Result<T> error(ResultCode resultCode) {
        Result<T> r = new Result<>();
        r.setCode(resultCode.getCode());
        r.setMessage(resultCode.getMessage());
        return r;
    }

    /** 失败, 自定义消息(状态码统一 FAIL) */
    public static <T> Result<T> error(String message) {
        Result<T> r = new Result<>();
        r.setCode(ResultCode.FAIL.getCode());
        r.setMessage(message);
        return r;
    }

    /** 失败, 自定义状态码 + 消息 */
    public static <T> Result<T> error(ResultCode resultCode, String message) {
        Result<T> r = new Result<>();
        r.setCode(resultCode.getCode());
        r.setMessage(message);
        return r;
    }

    /** 失败, 直接指定 code 与消息(用于 BusinessException) */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }
}
