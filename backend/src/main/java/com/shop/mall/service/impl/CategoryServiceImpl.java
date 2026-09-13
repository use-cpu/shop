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

    /** 分类树本地缓存有效期: 60 秒。分类数据量小、变更频率低, 热点接口无需每次全表查询 */
    private static final long TREE_CACHE_TTL_MS = 60_000L;
    private volatile List<CategoryVO> cachedTree;
    private volatile long cachedTreeExpireAt = 0L;

    @Override
    public List<CategoryVO> tree() {
        long now = System.currentTimeMillis();
        List<CategoryVO> cached = cachedTree;
        if (cached != null && now < cachedTreeExpireAt) {
            return cached;
        }
        List<CategoryVO> fresh = loadTree();
        cachedTree = fresh;
        cachedTreeExpireAt = now + TREE_CACHE_TTL_MS;
        return fresh;
    }

    /** 从数据库加载并构建两级分类树 */
    private List<CategoryVO> loadTree() {
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

    /** 分类发生增删改时主动失效缓存 */
    private void evictTreeCache() {
        cachedTree = null;
        cachedTreeExpireAt = 0L;
    }

    @Override
    public void add(Category category) {
        UserContext.requireAdmin();
        if (category.getStatus() == null) category.setStatus(1);
        if (category.getSort() == null) category.setSort(0);
        if (category.getParentId() == null) category.setParentId(0L);
        save(category);
        evictTreeCache();
    }

    @Override
    public void update(Category category) {
        UserContext.requireAdmin();
        updateById(category);
        evictTreeCache();
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
        evictTreeCache();
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
