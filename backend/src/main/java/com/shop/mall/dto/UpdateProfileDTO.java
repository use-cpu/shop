package com.shop.mall.dto;

import lombok.Data;

/**
 * 更新个人资料请求(手机号/邮箱/昵称)
 *
 * @author shop-mall
 */
@Data
public class UpdateProfileDTO {

    private String nickname;
    private String phone;
    private String email;
}
