package com.nextera.user;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 用户服务启动类
 *
 * @author nextera
 * @since 2025-06-16
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableDubbo(scanBasePackages = "com.nextera.user.dubbo")
@MapperScan("com.nextera.user.mapper")
@EnableScheduling
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
} 