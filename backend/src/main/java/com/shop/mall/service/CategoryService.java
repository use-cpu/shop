package com.shop.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.mall.entity.Category;
import com.shop.mall.vo.CategoryVO;

import java.util.List;

/**
 * 商品分类服务接口
 *
 * @author shop-mall
 */
public interface CategoryService extends IService<Category> {

    /** 获取分类树(一级→二级) */
    List<CategoryVO> tree();

    /** 新增分类(管理后台) */
    void add(Category category);

    /** 修改分类(管理后台) */
    void update(Category category);

    /** 删除分类(管理后台) */
    void remove(Long id);
}
