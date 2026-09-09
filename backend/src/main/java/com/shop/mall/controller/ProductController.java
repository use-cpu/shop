package com.shop.mall.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shop.mall.common.Result;
import com.shop.mall.dto.ProductDTO;
import com.shop.mall.dto.ProductQueryDTO;
import com.shop.mall.dto.StockUpdateDTO;
import com.shop.mall.service.ProductService;
import com.shop.mall.service.UserBehaviorService;
import com.shop.mall.vo.ProductVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品 Controller
 * 用户端: 列表/详情/热销; 管理端: 增删改查/上下架/库存
 *
 * @author shop-mall
 */
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserBehaviorService userBehaviorService;

    @GetMapping("/list")
    public Result<IPage<ProductVO>> list(ProductQueryDTO query) {
        return Result.success(productService.page(query));
    }

    @GetMapping("/hot")
    public Result<List<ProductVO>> hot() {
        return Result.success(productService.hot(8));
    }

    @GetMapping("/detail/{id}")
    public Result<ProductVO> detail(@PathVariable Long id) {
        ProductVO vo = productService.detail(id);
        // 记录浏览行为(用于个性化推荐)
        userBehaviorService.recordView(id, vo.getCategoryId());
        return Result.success(vo);
    }

    /* ==================== 管理后台 ==================== */
    @PostMapping
    public Result<Void> add(@Valid @RequestBody ProductDTO dto) {
        productService.add(dto);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@Valid @RequestBody ProductDTO dto) {
        productService.update(dto);
        return Result.success();
    }

    @PutMapping("/status/{id}/{status}")
    public Result<Void> toggleStatus(@PathVariable Long id, @PathVariable Integer status) {
        productService.toggleStatus(id, status);
        return Result.success();
    }

    @PutMapping("/stock")
    public Result<Void> updateStock(@Valid @RequestBody StockUpdateDTO dto) {
        productService.updateStock(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        productService.remove(id);
        return Result.success();
    }

    @GetMapping("/admin/list")
    public Result<IPage<ProductVO>> adminList(ProductQueryDTO query) {
        return Result.success(productService.pageAdmin(query));
    }
}
