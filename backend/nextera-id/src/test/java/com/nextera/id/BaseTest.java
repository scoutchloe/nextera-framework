package com.nextera.id;

import com.nextera.id.config.IdGeneratorConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import redis.embedded.RedisServer;

import jakarta.annotation.Resource;

/**
 * 基础测试类
 * 提供通用的测试配置和工具方法
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
@SpringBootTest(classes = IdApplication.class)
@SpringJUnitConfig
@ActiveProfiles("test")
public abstract class BaseTest {
    
    private static RedisServer redisServer;
    
    @Resource
    protected IdGeneratorConfig config;
    
    @BeforeAll
    static void setUpRedisServer() {
        try {
            redisServer = new RedisServer(6380);
            redisServer.start();
        } catch (Exception e) {
            System.err.println("Failed to start embedded Redis server: " + e.getMessage());
        }
    }
    
    @AfterAll
    static void tearDownRedisServer() {
        if (redisServer != null) {
            redisServer.stop();
        }
    }
    
    @BeforeEach
    void setUp() {
        // 初始化通用测试数据
        initTestData();
    }
    
    /**
     * 初始化测试数据
     */
    protected void initTestData() {
        // 子类可以重写此方法进行特定的数据初始化
    }
    
    /**
     * 生成测试业务类型
     */
    protected String generateTestBusinessType() {
        return "test-" + System.currentTimeMillis();
    }
    
    /**
     * 等待一段时间，用于测试异步操作
     */
    protected void waitForAsync() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
} 