package com.shop.mall.common;

import lombok.Getter;

/**
 * 自定义业务异常
 * Service 层抛出此异常, 由全局异常处理器统一捕获并返回 Result。
 * 使用方式: throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
 *
 * @author shop-mall
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
