package com.nextera.order.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;


/**
 * ShardingSphere 配置类
 * 用于确保自定义算法能被正确加载
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class ShardingConfiguration {

    @PostConstruct
    public void init() {
        log.info("ShardingSphere Configuration initialized");
        log.info("Custom Key Generator Type: {}", CustomKeyGenerator.TYPE);
        log.info("Custom Key Generator Class: {}", CustomKeyGenerator.class.getName());
        
        // 确保CustomKeyGenerator类被加载
        try {
            Class.forName(CustomKeyGenerator.class.getName());
            log.info("CustomKeyGenerator class loaded successfully");
        } catch (ClassNotFoundException e) {
            log.error("Failed to load CustomKeyGenerator class", e);
        }
    }
} 