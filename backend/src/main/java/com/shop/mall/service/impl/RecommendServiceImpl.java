package com.shop.mall.service.impl;

import com.shop.mall.entity.UserBehavior;
import com.shop.mall.mapper.UserBehaviorMapper;
import com.shop.mall.service.CategoryService;
import com.shop.mall.service.ProductService;
import com.shop.mall.service.RecommendService;
import com.shop.mall.utils.UserContext;
import com.shop.mall.vo.CategoryVO;
import com.shop.mall.vo.ProductVO;
import com.shop.mall.vo.RecommendVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 个性化推荐服务实现
 *
 * 算法(基于内容的推荐):
 *  1. 统计用户各分类权重之和, 取 Top3 偏好分类
 *  2. 从偏好分类查询在售商品(按销量降序)
 *  3. 过滤用户近期已浏览商品, 避免重复推荐
 *  4. 新用户或无行为 → 回退到热销榜
 *
 * @author shop-mall
 */
@Slf4j
@Service
public class RecommendServiceImpl implements RecommendService {

    /** 偏好分类数量 */
    private static final int PREFERRED_CATEGORY_LIMIT = 3;
    /** 每分类推荐数量 */
    private static final int PER_CATEGORY_LIMIT = 4;
    /** 热销榜数量 */
    private static final int HOT_LIMIT = 8;

    @Autowired private UserBehaviorMapper userBehaviorMapper;
    @Autowired private ProductService productService;
    @Autowired private CategoryService categoryService;

    @Override
    public RecommendVO homeRecommend() {
        RecommendVO vo = new RecommendVO();
        vo.setCategories(categoryService.tree());
        vo.setHot(productService.hot(HOT_LIMIT));

        Long userId = UserContext.getUserIdNullable();
        if (userId == null) {
            vo.setPersonalized(vo.getHot());   // 未登录: 复用热销
            return vo;
        }

        // 偏好分类 TopN
        List<Map<String, Object>> weights =
                userBehaviorMapper.selectCategoryWeights(userId, PREFERRED_CATEGORY_LIMIT);
        if (weights == null || weights.isEmpty()) {
            vo.setPersonalized(vo.getHot());   // 新用户无行为: 回退热销
            return vo;
        }

        List<Long> categoryIds = weights.stream()
                .map(m -> Long.valueOf(m.get("category_id").toString()))
                .collect(Collectors.toList());

        // 用户已浏览商品ID(避免重复推荐)
        List<UserBehavior> behaviors = userBehaviorMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<UserBehavior>()
                        .eq(UserBehavior::getUserId, userId)
                        .eq(UserBehavior::getBehaviorType, 1));
        List<Long> viewedIds = behaviors.stream()
                .map(UserBehavior::getProductId)
                .distinct()
                .collect(Collectors.toList());

        List<ProductVO> personalized = new ArrayList<>();
        for (Long cid : categoryIds) {
            List<ProductVO> products = productService.listByCategoryIds(
                    List.of(cid), PER_CATEGORY_LIMIT * 2);
            // 过滤已浏览, 取前 PER_CATEGORY_LIMIT
            products.stream()
                    .filter(p -> !viewedIds.contains(p.getId()))
                    .limit(PER_CATEGORY_LIMIT)
                    .forEach(personalized::add);
        }
        // 若过滤后为空, 回退热销
        if (personalized.isEmpty()) {
            vo.setPersonalized(vo.getHot());
        } else {
            vo.setPersonalized(personalized);
        }
        log.info("个性化推荐: userId={}, 偏好分类={}, 推荐{}件",
                userId, categoryIds, personalized.size());
        return vo;
    }
}
