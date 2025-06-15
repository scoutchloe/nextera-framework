package com.nextera.article;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 文章服务启动类
 *
 * @author nextera
 * @since 2025-06-16
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableDubbo
@EnableTransactionManagement
@MapperScan("com.nextera.article.mapper")
public class ArticleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArticleApplication.class, args);
        System.out.println("========================================");
        System.out.println("    Nextera Article Service Started   ");
        System.out.println("========================================");
    }
} 