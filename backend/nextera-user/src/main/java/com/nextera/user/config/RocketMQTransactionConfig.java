package com.nextera.user.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * RocketMQ事务配置（最简化版本）
 * 避免所有可能的循环依赖，使用最基本的配置方式
 * 
 * @author nextera
 * @since 2025-06-23
 */
@Slf4j
@Configuration
public class RocketMQTransactionConfig {

    /**
     * RocketMQ专用事务管理器（最简化版本）
     * 避免循环依赖的最终方案
     */
//    @Bean("rocketmqTransactionManager")
//    @ConditionalOnMissingBean(name = "rocketmqTransactionManager")
//    public PlatformTransactionManager rocketmqTransactionManager(DataSource dataSource) {
//        log.info("=== 创建RocketMQ专用事务管理器（最简化版本） ===");
//
//        DataSourceTransactionManager manager = new DataSourceTransactionManager(dataSource);
//        manager.setDefaultTimeout(60);
//        manager.setRollbackOnCommitFailure(true);
//
//        log.info("RocketMQ专用事务管理器创建完成");
//        return manager;
//    }
} 