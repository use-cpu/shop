package com.shop.mall.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.mall.common.BusinessException;
import com.shop.mall.common.ResultCode;
import com.shop.mall.dto.ProductDTO;
import com.shop.mall.dto.ProductQueryDTO;
import com.shop.mall.dto.StockUpdateDTO;
import com.shop.mall.entity.Category;
import com.shop.mall.entity.Product;
import com.shop.mall.mapper.CategoryMapper;
import com.shop.mall.mapper.ProductMapper;
import com.shop.mall.service.ProductService;
import com.shop.mall.service.StockService;
import com.shop.mall.utils.UserContext;
import com.shop.mall.vo.ProductVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品服务实现
 *
 * @author shop-mall
 */
@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private StockService stockService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${shop.stock.prefix}")
    private String stockPrefix;

    @Override
    public IPage<ProductVO> page(ProductQueryDTO query) {
        return doPage(query, true);
    }

    @Override
    public IPage<ProductVO> pageAdmin(ProductQueryDTO query) {
        return doPage(query, false);
    }

    private IPage<ProductVO> doPage(ProductQueryDTO query, boolean onlyOnline) {
        Page<Product> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<Product> w = new LambdaQueryWrapper<>();
        if (onlyOnline) {
            w.eq(Product::getStatus, 1);
        }
        if (StringUtils.hasText(query.getKeyword())) {
            w.and(q -> q.like(Product::getName, query.getKeyword())
                    .or().like(Product::getSubtitle, query.getKeyword()));
        }
        if (query.getCategoryId() != null) {
            // 父级分类需包含其所有子分类下的商品
            List<Long> categoryIds = new ArrayList<>();
            categoryIds.add(query.getCategoryId());
            List<Category> children = categoryMapper.selectList(
                    new LambdaQueryWrapper<Category>().eq(Category::getParentId, query.getCategoryId()));
            children.forEach(c -> categoryIds.add(c.getId()));
            w.in(Product::getCategoryId, categoryIds);
        }
        // 排序
        if ("price_asc".equals(query.getSort())) {
            w.orderByAsc(Product::getPrice);
        } else if ("price_desc".equals(query.getSort())) {
            w.orderByDesc(Product::getPrice);
        } else if ("sales_desc".equals(query.getSort())) {
            w.orderByDesc(Product::getSales);
        } else if ("new_desc".equals(query.getSort())) {
            // 新品优先: 按商品 id 倒序(最新上架在前)
            w.orderByDesc(Product::getId);
        } else {
            w.orderByDesc(Product::getUpdateTime);
        }
        IPage<Product> p = page(page, w);
        // 批量预加载分类名, 避免 toVO 中 N+1 查询
        Map<Long, String> categoryNameMap = loadCategoryNames(p.getRecords());
        return p.convert(prod -> toVO(prod, categoryNameMap));
    }

    /** 批量查询商品所属分类名, 构建 categoryId→name 映射 */
    private Map<Long, String> loadCategoryNames(List<Product> products) {
        if (products == null || products.isEmpty()) return Collections.emptyMap();
        List<Long> ids = products.stream().map(Product::getCategoryId).distinct().collect(Collectors.toList());
        List<Category> cats = categoryMapper.selectBatchIds(ids);
        return cats.stream().collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));
    }

    @Override
    public ProductVO detail(Long id) {
        Product p = getById(id);
        if (p == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        return toVO(p);
    }

    @Override
    public List<ProductVO> hot(int limit) {
        Page<Product> page = new Page<>(1, limit);
        LambdaQueryWrapper<Product> w = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getSales);
        List<Product> records = page(page, w).getRecords();
        Map<Long, String> nameMap = loadCategoryNames(records);
        return records.stream().map(p -> toVO(p, nameMap)).collect(Collectors.toList());
    }

    @Override
    public List<ProductVO> listByCategoryIds(List<Long> categoryIds, int limit) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return Collections.emptyList();
        }
        Page<Product> page = new Page<>(1, limit);
        LambdaQueryWrapper<Product> w = new LambdaQueryWrapper<Product>()
                .in(Product::getCategoryId, categoryIds)
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getSales);
        List<Product> records = page(page, w).getRecords();
        Map<Long, String> nameMap = loadCategoryNames(records);
        return records.stream().map(p -> toVO(p, nameMap)).collect(Collectors.toList());
    }

    @Override
    public void add(ProductDTO dto) {
        UserContext.requireAdmin();
        Product p = new Product();
        BeanUtil.copyProperties(dto, p);
        if (p.getStock() == null) p.setStock(0);
        if (p.getSales() == null) p.setSales(0);
        if (p.getStatus() == null) p.setStatus(1);
        p.setVersion(0);
        save(p);
        // 同步库存到 Redis
        stockService.initStock(p.getId());
    }

    @Override
    public void update(ProductDTO dto) {
        UserContext.requireAdmin();
        Product p = getById(dto.getId());
        if (p == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        BeanUtil.copyProperties(dto, p);
        // 不直接覆盖 version, 交给 MyBatis-Plus 乐观锁
        updateById(p);
        stockService.initStock(p.getId());
    }

    @Override
    public void toggleStatus(Long id, Integer status) {
        UserContext.requireAdmin();
        Product p = getById(id);
        if (p == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        p.setStatus(status);
        updateById(p);
    }

    @Override
    public void updateStock(StockUpdateDTO dto) {
        UserContext.requireAdmin();
        Product p = getById(dto.getProductId());
        if (p == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        p.setStock(dto.getStock());
        updateById(p);
        stockService.initStock(p.getId());
    }

    @Override
    public void remove(Long id) {
        UserContext.requireAdmin();
        Product p = getById(id);
        if (p == null) return;
        removeById(id);
        // 清除库存缓存
        redisTemplate.delete(stockPrefix + id);
    }

    @Override
    public void increaseSales(Long productId, int qty) {
        update(null, new LambdaUpdateWrapper<Product>()
                .eq(Product::getId, productId)
                .setSql("sales = sales + " + qty));
    }

    @Override
    public void decreaseSales(Long productId, int qty) {
        update(null, new LambdaUpdateWrapper<Product>()
                .eq(Product::getId, productId)
                .setSql("sales = sales - " + qty)
                .setSql("sales = IF(sales < 0, 0, sales)"));
    }

    /** 实体转视图(单条, 分类名按需查询) */
    private ProductVO toVO(Product p) {
        return toVO(p, Collections.emptyMap());
    }

    /** 实体转视图(支持批量预加载的分类名映射, 避免 N+1) */
    private ProductVO toVO(Product p, Map<Long, String> categoryNameMap) {
        ProductVO vo = new ProductVO();
        vo.setId(p.getId());
        vo.setCategoryId(p.getCategoryId());
        // 优先使用预加载映射, 兜底查询单条
        String catName = categoryNameMap.get(p.getCategoryId());
        if (catName == null) {
            Category cat = categoryMapper.selectById(p.getCategoryId());
            catName = cat == null ? "" : cat.getName();
        }
        vo.setCategoryName(catName);
        vo.setName(p.getName());
        vo.setSubtitle(p.getSubtitle());
        vo.setMainImage(p.getMainImage());
        // 多图逗号分隔 → 列表
        if (StringUtils.hasText(p.getImages())) {
            vo.setImages(Arrays.asList(p.getImages().split(",")));
        } else {
            vo.setImages(Collections.emptyList());
        }
        vo.setDetail(p.getDetail());
        vo.setPrice(p.getPrice());
        vo.setOriginalPrice(p.getOriginalPrice());
        vo.setStock(p.getStock());
        vo.setSales(p.getSales());
        vo.setStatus(p.getStatus());
        vo.setVersion(p.getVersion());
        return vo;
    }
}
