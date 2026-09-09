package com.shop.mall.controller;

import com.shop.mall.common.Result;
import com.shop.mall.dto.AddressDTO;
import com.shop.mall.entity.Address;
import com.shop.mall.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收货地址 Controller
 *
 * @author shop-mall
 */
@RestController
@RequestMapping("/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping
    public Result<List<Address>> list() {
        return Result.success(addressService.listMine());
    }

    @PostMapping
    public Result<Void> add(@Valid @RequestBody AddressDTO dto) {
        addressService.add(dto);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody AddressDTO dto) {
        addressService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        addressService.remove(id);
        return Result.success();
    }

    @PutMapping("/default/{id}")
    public Result<Void> setDefault(@PathVariable Long id) {
        addressService.setDefault(id);
        return Result.success();
    }
}
