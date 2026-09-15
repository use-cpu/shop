package com.shop.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop.mall.common.Result;
import com.shop.mall.dto.UserStatusDTO;
import com.shop.mall.entity.User;
import com.shop.mall.service.UserService;
import com.shop.mall.utils.UserContext;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理 Controller(管理后台)
 *
 * @author shop-mall
 */
@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Result<Page<User>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        UserContext.requireAdmin();
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getPhone, keyword);
        }
        w.orderByDesc(User::getCreateTime);
        Page<User> result = userService.page(page, w);
        // 脱敏
        result.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(result);
    }

    @PutMapping("/status")
    public Result<Void> updateStatus(@Valid @RequestBody UserStatusDTO dto) {
        UserContext.requireAdmin();
        userService.updateStatus(dto.getUserId(), dto.getStatus());
        return Result.success();
    }
}
