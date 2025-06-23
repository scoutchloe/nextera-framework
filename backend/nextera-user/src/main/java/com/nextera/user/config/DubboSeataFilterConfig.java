package com.nextera.user.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ConsumerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Dubbo Seata Filter配置
 * 专门用于处理RocketMQ事务监听器与Seata的兼容性问题
 * 在RocketMQ事务环境中排除Seata filter，避免branchType为null的错误
 *
 * @author nextera
 * @since 2025-06-23
 */
@Slf4j
@Configuration
public class DubboSeataFilterConfig {

//    /**
//     * 专门用于RocketMQ场景的Dubbo消费者配置
//     * 排除所有Seata相关过滤器，防止在RocketMQ事务监听器中出现branchType为null错误
//     */
//    @Bean("rocketmqDubboConsumerConfig")
//    public ConsumerConfig rocketmqDubboConsumerConfig() {
//        ConsumerConfig consumerConfig = new ConsumerConfig();
//        consumerConfig.setTimeout(5000);
//        consumerConfig.setRetries(0);
//        consumerConfig.setCheck(false);
//
//        // 排除所有可能导致branchType为null的Seata相关过滤器
//        String excludeFilters = "-seataTransactionPropagation,-seata,-seataFilter,-transaction";
//        consumerConfig.setFilter(excludeFilters);
//
//        log.info("配置RocketMQ专用Dubbo消费者，排除Seata过滤器: {}", excludeFilters);
//
//        return consumerConfig;
//    }
//
//    /**
//     * 普通业务场景的Dubbo消费者配置
//     * 保留Seata功能，用于正常的分布式事务场景
//     */
//    @Bean("normalDubboConsumerConfig")
//    @Primary
//    public ConsumerConfig normalDubboConsumerConfig() {
//        ConsumerConfig consumerConfig = new ConsumerConfig();
//        consumerConfig.setTimeout(5000);
//        consumerConfig.setRetries(0);
//        consumerConfig.setCheck(false);
//
//        log.info("配置普通业务Dubbo消费者，保留Seata功能");
//
//        return consumerConfig;
//    }
} 