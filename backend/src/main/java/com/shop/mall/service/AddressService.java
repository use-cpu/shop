package com.shop.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop.mall.dto.AddressDTO;
import com.shop.mall.entity.Address;

import java.util.List;

/**
 * 收货地址服务接口
 *
 * @author shop-mall
 */
public interface AddressService extends IService<Address> {

    /** 查询当前用户全部地址 */
    List<Address> listMine();

    /** 新增地址 */
    void add(AddressDTO dto);

    /** 修改地址 */
    void update(AddressDTO dto);

    /** 删除地址 */
    void remove(Long id);

    /** 设为默认 */
    void setDefault(Long id);

    /** 获取用户默认地址 */
    Address getDefault(Long userId);
}
