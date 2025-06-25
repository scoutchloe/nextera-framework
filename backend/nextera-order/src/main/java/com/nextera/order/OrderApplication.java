package com.nextera.order;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 订单服务启动类
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
@SpringBootApplication
@EnableDubbo(scanBasePackages = "com.nextera.order.dubbo.impl")
@MapperScan("com.nextera.order.mapper")
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
} 