package com.shop.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.mall.common.BusinessException;
import com.shop.mall.common.ResultCode;
import com.shop.mall.entity.Category;
import com.shop.mall.mapper.CategoryMapper;
import com.shop.mall.service.CategoryService;
import com.shop.mall.utils.UserContext;
import com.shop.mall.vo.CategoryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品分类服务实现
 *
 * @author shop-mall
 */
@Slf4j
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public List<CategoryVO> tree() {
        List<Category> all = list(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSort));
        // 一级分类
        List<CategoryVO> roots = all.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0)
                .map(this::toVO)
                .collect(Collectors.toList());
        // 挂载二级
        for (CategoryVO root : roots) {
            List<CategoryVO> children = all.stream()
                    .filter(c -> root.getId().equals(c.getParentId()))
                    .map(this::toVO)
                    .collect(Collectors.toList());
            root.setChildren(children);
        }
        return roots;
    }

    @Override
    public void add(Category category) {
        UserContext.requireAdmin();
        if (category.getStatus() == null) category.setStatus(1);
        if (category.getSort() == null) category.setSort(0);
        if (category.getParentId() == null) category.setParentId(0L);
        save(category);
    }

    @Override
    public void update(Category category) {
        UserContext.requireAdmin();
        updateById(category);
    }

    @Override
    public void remove(Long id) {
        UserContext.requireAdmin();
        // 简单校验: 是否有子分类
        long childCount = count(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException(ResultCode.FAIL, "该分类下有子分类, 无法删除");
        }
        removeById(id);
    }

    private CategoryVO toVO(Category c) {
        CategoryVO vo = new CategoryVO();
        vo.setId(c.getId());
        vo.setName(c.getName());
        vo.setParentId(c.getParentId());
        vo.setSort(c.getSort());
        vo.setIcon(c.getIcon());
        vo.setStatus(c.getStatus());
        vo.setChildren(new ArrayList<>());
        return vo;
    }
}
