package com.shop.mall.controller;

import com.shop.mall.common.Result;
import com.shop.mall.entity.Category;
import com.shop.mall.service.CategoryService;
import com.shop.mall.vo.CategoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类 Controller
 *
 * @author shop-mall
 */
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/tree")
    public Result<List<CategoryVO>> tree() {
        return Result.success(categoryService.tree());
    }

    @PostMapping
    public Result<Void> add(@RequestBody Category category) {
        categoryService.add(category);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Category category) {
        categoryService.update(category);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        categoryService.remove(id);
        return Result.success();
    }
}
