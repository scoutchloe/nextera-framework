package com.nextera.user.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TCC 配置类
 * 包含TCC所需的配置项
 */
@Configuration
public class TccConfiguration {

    /**
     * 配置ObjectMapper支持Java 8时间类型
     * 用于TCC状态序列化
     */
    @Bean("tccObjectMapper") 
    public ObjectMapper tccObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 注册Java 8时间模块
        mapper.registerModule(new JavaTimeModule());
        // 禁用时间戳格式，使用ISO-8601格式
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
} 