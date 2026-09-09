package com.shop.mall.controller;

import com.shop.mall.common.Result;
import com.shop.mall.dto.LoginDTO;
import com.shop.mall.dto.RegisterDTO;
import com.shop.mall.entity.User;
import com.shop.mall.service.UserService;
import com.shop.mall.vo.LoginVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 鉴权 Controller
 * 注册 / 登录 / 当前用户信息 / 管理员登录
 *
 * @author shop-mall
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @PostMapping("/admin/login")
    public Result<LoginVO> adminLogin(@Valid @RequestBody LoginDTO dto) {
        return Result.success(userService.adminLogin(dto));
    }

    @GetMapping("/info")
    public Result<User> info() {
        return Result.success(userService.getCurrentUser());
    }
}
