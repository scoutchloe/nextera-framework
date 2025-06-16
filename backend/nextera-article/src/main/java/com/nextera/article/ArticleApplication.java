package com.nextera.article;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 文章服务启动类
 *
 * @author nextera
 * @since 2025-06-16
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableDubbo
public class ArticleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArticleApplication.class, args);
    }
} 