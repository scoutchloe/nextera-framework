package com.nextera.managenextera.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类
 *
 * @author nextera
 * @since 2025-01-01
 */
@Slf4j
@Configuration
public class RedisConfig {

    /**
     * 配置支持Java 8时间类型的ObjectMapper
     * 
     * @return ObjectMapper
     */
    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 注册Java 8时间模块
        objectMapper.registerModule(new JavaTimeModule());
        
        // 禁用将时间写为时间戳的功能
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        log.info("Redis ObjectMapper配置完成，支持Java 8时间类型");
        return objectMapper;
    }

    /**
     * 配置RedisTemplate
     * 
     * @param connectionFactory Redis连接工厂
     * @param redisObjectMapper 支持Java 8时间类型的ObjectMapper
     * @return RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory, 
                                                       ObjectMapper redisObjectMapper) {
        log.info("初始化RedisTemplate配置");
        
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 设置序列化方式
        // 使用String序列化器序列化Key
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);

        // 使用支持Java 8时间类型的JSON序列化器序列化Value
        GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer = 
            new GenericJackson2JsonRedisSerializer(redisObjectMapper);
        template.setValueSerializer(genericJackson2JsonRedisSerializer);
        template.setHashValueSerializer(genericJackson2JsonRedisSerializer);

        // 设置默认序列化器
        template.setDefaultSerializer(genericJackson2JsonRedisSerializer);

        // 初始化
        template.afterPropertiesSet();

        log.info("RedisTemplate配置完成");
        return template;
    }
} 