package com.shop.mall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 电商购物商城系统 启动类
 *
 * @author shop-mall
 */
@SpringBootApplication
@MapperScan("com.shop.mall.mapper")   // 扫描 MyBatis-Plus Mapper 接口
@EnableScheduling                     // 开启定时任务: 订单超时自动关单
public class ShopMallApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopMallApplication.class, args);
        System.out.println("====== 电商购物商城后端启动成功 ======");
    }
}
