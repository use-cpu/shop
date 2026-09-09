package com.shop.mall.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户状态更新请求(管理后台)
 *
 * @author shop-mall
 */
@Data
public class UserStatusDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
