package com.shop.mall.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.mall.dto.ProductDTO;
import com.shop.mall.dto.ProductQueryDTO;
import com.shop.mall.dto.StockUpdateDTO;
import com.shop.mall.entity.Product;
import com.shop.mall.vo.ProductVO;

import java.util.List;

/**
 * 商品服务接口
 *
 * @author shop-mall
 */
public interface ProductService extends IService<Product> {

    /** 分页查询(用户端: 仅上架) */
    IPage<ProductVO> page(ProductQueryDTO query);

    /** 商品详情 */
    ProductVO detail(Long id);

    /** 热销商品 */
    List<ProductVO> hot(int limit);

    /** 新增商品(管理后台) */
    void add(ProductDTO dto);

    /** 修改商品(管理后台) */
    void update(ProductDTO dto);

    /** 上下架(管理后台) */
    void toggleStatus(Long id, Integer status);

    /** 更新库存(管理后台) */
    void updateStock(StockUpdateDTO dto);

    /** 删除商品(管理后台) */
    void remove(Long id);

    /** 分页查询全部商品(管理后台: 含下架) */
    IPage<ProductVO> pageAdmin(ProductQueryDTO query);

    /** 根据分类ID集合查询在售商品(推荐用) */
    List<ProductVO> listByCategoryIds(List<Long> categoryIds, int limit);

    /** 扣减销量(下单后) */
    void increaseSales(Long productId, int qty);

    /** 回滚销量(关单/取消) */
    void decreaseSales(Long productId, int qty);
}
