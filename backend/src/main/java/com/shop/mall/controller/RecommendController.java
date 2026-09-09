package com.shop.mall.controller;

import com.shop.mall.common.Result;
import com.shop.mall.service.RecommendService;
import com.shop.mall.vo.RecommendVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 个性化推荐 Controller
 *
 * @author shop-mall
 */
@RestController
@RequestMapping("/recommend")
public class RecommendController {

    @Autowired
    private RecommendService recommendService;

    @GetMapping
    public Result<RecommendVO> home() {
        return Result.success(recommendService.homeRecommend());
    }
}
