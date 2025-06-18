package com.nextera.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 认证服务启动类
 *
 * @author Nextera
 */
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.nextera.auth", "com.nextera.common"})
public class AuthApplication {
    public static void main(String[] args) {
        // 禁用Nacos默认日志配置，避免冲突
        System.setProperty("nacos.logging.default.config.enabled", "false");
        System.setProperty("nacos.logging.config", "");
        
        SpringApplication.run(AuthApplication.class, args);
        System.out.println("Nextera Auth Service started successfully!");
    }
} 