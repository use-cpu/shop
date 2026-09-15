package com.shop.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.mall.dto.LoginDTO;
import com.shop.mall.dto.RegisterDTO;
import com.shop.mall.dto.UpdateProfileDTO;
import com.shop.mall.entity.User;
import com.shop.mall.vo.LoginVO;

/**
 * 用户服务接口
 *
 * @author shop-mall
 */
public interface UserService extends IService<User> {

    /** 用户注册 */
    void register(RegisterDTO dto);

    /** 用户登录 */
    LoginVO login(LoginDTO dto);

    /** 管理员登录(校验角色) */
    LoginVO adminLogin(LoginDTO dto);

    /** 获取当前登录用户信息 */
    User getCurrentUser();

    /** 更新个人资料(昵称/手机号/邮箱) */
    void updateProfile(UpdateProfileDTO dto);

    /** 用户状态更新(管理后台) */
    void updateStatus(Long userId, Integer status);
}
