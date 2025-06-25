package com.nextera.id.config;

import com.nextera.idapi.enums.IdGeneratorType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * ID生成器配置
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "nextera.id")
public class IdGeneratorConfig {
    
    /**
     * 默认ID生成策略
     */
    private IdGeneratorType defaultStrategy = IdGeneratorType.REDIS;
    
    /**
     * Redis配置
     */
    private RedisConfig redis = new RedisConfig();
    
    /**
     * PostgreSQL配置
     */
    private PostgresqlConfig postgresql = new PostgresqlConfig();
    
    /**
     * 业务类型配置
     */
    private Map<String, BusinessTypeConfig> businessTypes = new HashMap<>();
    
    @Data
    public static class RedisConfig {
        private String host = "localhost";
        private int port = 6379;
        private String password;
        private int database = 0;
        private int batchSize = 100;
        private int expireTime = 3600;
    }
    
    @Data
    public static class PostgresqlConfig {
        private String url = "jdbc:postgresql://localhost:5432/nextera_id";
        private String username = "postgres";
        private String password = "123456";
        private int batchSize = 1000;
    }
    
    @Data
    public static class BusinessTypeConfig {
        private IdGeneratorType strategy = IdGeneratorType.REDIS;
        private Integer batchSize = 100;
        private Long startValue = 1L;
    }
    
    /**
     * 获取业务类型配置
     */
    public BusinessTypeConfig getBusinessTypeConfig(String businessType) {
        return businessTypes.getOrDefault(businessType, createDefaultBusinessTypeConfig());
    }
    
    /**
     * 创建默认业务类型配置
     */
    private BusinessTypeConfig createDefaultBusinessTypeConfig() {
        BusinessTypeConfig config = new BusinessTypeConfig();
        config.setStrategy(defaultStrategy);
        config.setBatchSize(defaultStrategy == IdGeneratorType.REDIS ? redis.getBatchSize() : postgresql.getBatchSize());
        config.setStartValue(1L);
        return config;
    }
    
    /**
     * 获取业务类型的生成策略
     */
    public IdGeneratorType getBusinessTypeStrategy(String businessType) {
        if (businessType == null || businessType.trim().isEmpty()) {
            return defaultStrategy;
        }
        BusinessTypeConfig config = getBusinessTypeConfig(businessType);
        return config.getStrategy() != null ? config.getStrategy() : defaultStrategy;
    }
    
    /**
     * 获取业务类型的批量大小
     */
    public Integer getBusinessTypeBatchSize(String businessType) {
        if (businessType == null || businessType.trim().isEmpty()) {
            return defaultStrategy == IdGeneratorType.REDIS ? redis.getBatchSize() : postgresql.getBatchSize();
        }
        BusinessTypeConfig config = getBusinessTypeConfig(businessType);
        return config.getBatchSize() != null ? config.getBatchSize() : 
               (config.getStrategy() == IdGeneratorType.REDIS ? redis.getBatchSize() : postgresql.getBatchSize());
    }
    
    /**
     * 获取业务类型的起始值
     */
    public Long getBusinessTypeStartValue(String businessType) {
        if (businessType == null || businessType.trim().isEmpty()) {
            return 1L;
        }
        BusinessTypeConfig config = getBusinessTypeConfig(businessType);
        return config.getStartValue() != null ? config.getStartValue() : 1L;
    }
} 