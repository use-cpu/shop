package com.shop.mall.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 收货地址请求(新增/修改)
 *
 * @author shop-mall
 */
@Data
public class AddressDTO {

    private Long id;

    @NotBlank(message = "收货人不能为空")
    private String receiverName;

    @NotBlank(message = "收货电话不能为空")
    private String receiverPhone;

    private String province;
    private String city;
    private String district;

    @NotBlank(message = "详细地址不能为空")
    private String detail;

    /** 0否 1默认 */
    private Integer isDefault = 0;
}
