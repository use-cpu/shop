package com.shop.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop.mall.common.BusinessException;
import com.shop.mall.common.ResultCode;
import com.shop.mall.dto.AddressDTO;
import com.shop.mall.entity.Address;
import com.shop.mall.mapper.AddressMapper;
import com.shop.mall.service.AddressService;
import com.shop.mall.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 收货地址服务实现
 *
 * @author shop-mall
 */
@Slf4j
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    @Override
    public List<Address> listMine() {
        Long userId = UserContext.getUserId();
        return list(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getUpdateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(AddressDTO dto) {
        Long userId = UserContext.getUserId();
        Address addr = new Address();
        addr.setUserId(userId);
        addr.setReceiverName(dto.getReceiverName());
        addr.setReceiverPhone(dto.getReceiverPhone());
        addr.setProvince(dto.getProvince());
        addr.setCity(dto.getCity());
        addr.setDistrict(dto.getDistrict());
        addr.setDetail(dto.getDetail());
        addr.setIsDefault(dto.getIsDefault() == null ? 0 : dto.getIsDefault());
        // 设为默认时, 先清除原默认
        if (addr.getIsDefault() == 1) {
            clearDefault(userId);
        }
        save(addr);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(AddressDTO dto) {
        Long userId = UserContext.getUserId();
        Address addr = getById(dto.getId());
        if (addr == null || !addr.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND);
        }
        addr.setReceiverName(dto.getReceiverName());
        addr.setReceiverPhone(dto.getReceiverPhone());
        addr.setProvince(dto.getProvince());
        addr.setCity(dto.getCity());
        addr.setDistrict(dto.getDistrict());
        addr.setDetail(dto.getDetail());
        if (dto.getIsDefault() != null && dto.getIsDefault() == 1) {
            clearDefault(userId);
            addr.setIsDefault(1);
        }
        updateById(addr);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        Long userId = UserContext.getUserId();
        Address addr = getById(id);
        if (addr == null || !addr.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND);
        }
        removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long id) {
        Long userId = UserContext.getUserId();
        Address addr = getById(id);
        if (addr == null || !addr.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND);
        }
        clearDefault(userId);
        addr.setIsDefault(1);
        updateById(addr);
    }

    @Override
    public Address getDefault(Long userId) {
        return getOne(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, 1)
                .last("LIMIT 1"));
    }

    /** 清除用户原默认地址 */
    private void clearDefault(Long userId) {
        update(new LambdaUpdateWrapper<Address>()
                .eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, 1)
                .set(Address::getIsDefault, 0));
    }
}
