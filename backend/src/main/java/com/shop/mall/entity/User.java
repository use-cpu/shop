package com.shop.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体, 对应 user 表
 *
 * @author shop-mall
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;
    private String password;
    private String nickname;
    private String phone;
    private String email;
    private String avatar;

    /** 0普通用户 1管理员 */
    private Integer role;

    /** 0禁用 1正常 */
    private Integer status;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
