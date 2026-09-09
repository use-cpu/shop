package com.shop.mall.controller;

import com.shop.mall.common.Result;
import com.shop.mall.dto.CartDTO;
import com.shop.mall.dto.CartSelectDTO;
import com.shop.mall.service.CartService;
import com.shop.mall.vo.CartVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车 Controller(数据存于 Redis)
 *
 * @author shop-mall
 */
@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public Result<CartVO> list() {
        return Result.success(cartService.list());
    }

    @PostMapping
    public Result<Void> add(@Valid @RequestBody CartDTO dto) {
        cartService.add(dto);
        return Result.success();
    }

    @PutMapping("/quantity")
    public Result<Void> updateQuantity(@Valid @RequestBody CartDTO dto) {
        cartService.updateQuantity(dto);
        return Result.success();
    }

    @PutMapping("/select")
    public Result<Void> toggleSelect(@Valid @RequestBody CartSelectDTO dto) {
        cartService.toggleSelect(dto);
        return Result.success();
    }

    @PutMapping("/select-all")
    public Result<Void> toggleSelectAll(@RequestParam Boolean selected) {
        cartService.toggleSelectAll(selected);
        return Result.success();
    }

    @DeleteMapping
    public Result<Void> remove(@RequestBody List<Long> productIds) {
        cartService.remove(productIds);
        return Result.success();
    }

    @PostMapping("/merge")
    public Result<Void> merge(@RequestParam String deviceToken) {
        cartService.merge(deviceToken);
        return Result.success();
    }
}
