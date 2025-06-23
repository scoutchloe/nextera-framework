package com.nextera.user.config;

import com.nextera.api.article.service.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RocketMQ专用的Dubbo配置
 * 完全绕过Seata，解决branchType为null的问题
 *
 * @author nextera
 * @since 2025-06-23
 */
@Slf4j
@Configuration
public class RocketMQDubboConfig {

    @Value("${dubbo.registry.address:nacos://localhost:8848}")
    private String registryAddress;

    /**
     * 为RocketMQ专门创建的Dubbo Reference
     * 完全绕过Seata的自动配置和过滤器
     */
    @Bean("rocketmqArticleService")
    public ArticleService rocketmqArticleService() {
        log.info("=== 创建RocketMQ专用的Dubbo Reference，完全绕过Seata ===");
        
        // 创建注册中心配置
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(registryAddress);
        
        // 创建Reference配置
        ReferenceConfig<ArticleService> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setInterface(ArticleService.class);
        referenceConfig.setVersion("1.0.0");
        referenceConfig.setCheck(false);
        referenceConfig.setTimeout(5000);
        referenceConfig.setRegistry(registryConfig);
        
        // 关键：完全禁用所有过滤器，包括Seata相关的
        referenceConfig.setFilter("-default,-seata,-seataTransactionPropagation,-transaction,-seataFilter");
        
        // 设置应用名称
        referenceConfig.setApplication(new org.apache.dubbo.config.ApplicationConfig("rocketmq-article-client"));
        
        log.info("RocketMQ专用Dubbo Reference配置: interface={}, version={}, filter={}", 
                ArticleService.class.getName(), "1.0.0", referenceConfig.getFilter());
        
        // 获取代理对象
        ArticleService articleService = referenceConfig.get();
        
        log.info("=== RocketMQ专用Dubbo Reference创建成功，已绕过所有Seata过滤器 ===");
        
        return articleService;
    }
} 