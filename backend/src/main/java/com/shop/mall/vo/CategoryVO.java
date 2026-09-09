package com.shop.mall.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 分类树节点(支持多级)
 *
 * @author shop-mall
 */
@Data
public class CategoryVO {

    private Long id;
    private String name;
    private Long parentId;
    private Integer sort;
    private String icon;
    private Integer status;

    /** 子分类 */
    private List<CategoryVO> children = new ArrayList<>();
}
