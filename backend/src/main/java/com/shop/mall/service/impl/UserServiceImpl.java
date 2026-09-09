package com.shop.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.mall.common.BusinessException;
import com.shop.mall.common.ResultCode;
import com.shop.mall.dto.LoginDTO;
import com.shop.mall.dto.RegisterDTO;
import com.shop.mall.entity.User;
import com.shop.mall.mapper.UserMapper;
import com.shop.mall.service.UserService;
import com.shop.mall.utils.JwtUtils;
import com.shop.mall.utils.PasswordUtils;
import com.shop.mall.utils.UserContext;
import com.shop.mall.vo.LoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 *
 * @author shop-mall
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public void register(RegisterDTO dto) {
        // 校验用户名是否已存在
        long count = count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw new BusinessException(ResultCode.USER_EXIST);
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(PasswordUtils.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setRole(0);
        user.setStatus(1);
        save(user);
        log.info("用户注册成功: {}", user.getUsername());
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (!PasswordUtils.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        return buildLoginVO(user);
    }

    @Override
    public LoginVO adminLogin(LoginDTO dto) {
        LoginVO vo = login(dto);
        // 校验管理员角色
        if (vo.getRole() == null || vo.getRole() != 1) {
            throw new BusinessException(ResultCode.ADMIN_REQUIRED);
        }
        return vo;
    }

    @Override
    public User getCurrentUser() {
        Long userId = UserContext.getUserId();
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        // 脱敏: 不返回密码
        user.setPassword(null);
        return user;
    }

    @Override
    public void updateStatus(Long userId, Integer status) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        user.setStatus(status);
        updateById(user);
    }

    private LoginVO buildLoginVO(User user) {
        LoginVO vo = new LoginVO();
        vo.setToken(jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole()));
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        return vo;
    }
}
