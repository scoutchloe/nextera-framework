package com.nextera.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 简单的用户服务启动类 - 用于测试
 *
 * @author nextera
 * @since 2025-06-16
 */
@SpringBootApplication
@RestController
public class SimpleUserApplication {

    @GetMapping("/health")
    public String health() {
        return "Nextera User Service is running!";
    }

    public static void main(String[] args) {
        SpringApplication.run(SimpleUserApplication.class, args);
        System.out.println("Simple Nextera User Service started successfully!");
    }
} 