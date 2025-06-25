package com.nextera.id;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * ID生成服务应用启动类
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableDubbo
public class IdApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdApplication.class, args);
        System.out.println("========================");
        System.out.println("Nextera ID服务启动成功");
        System.out.println("========================");
    }
} 