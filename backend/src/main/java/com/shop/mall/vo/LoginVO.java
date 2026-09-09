package com.shop.mall.vo;

import lombok.Data;

/**
 * 登录返回: JWT token + 用户基本信息
 *
 * @author shop-mall
 */
@Data
public class LoginVO {

    private String token;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;

    /** 0普通用户 1管理员 */
    private Integer role;
}
