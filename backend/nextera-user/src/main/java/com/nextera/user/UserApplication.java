package com.nextera.user;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 用户服务启动类
 *
 * @author Nextera
 */
@EnableFeignClients
@EnableDiscoveryClient
@EnableDubbo
@SpringBootApplication(scanBasePackages = {"com.nextera.user", "com.nextera.common"})
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
        System.out.println("Nextera User Service started successfully!");
    }
} 