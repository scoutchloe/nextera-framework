package com.nextera.auth;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import redis.embedded.RedisServer;

/**
 * 测试基础类
 *
 * @author Nextera
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
public abstract class BaseTest {

    private static RedisServer redisServer;

    static {
        try {
            redisServer = new RedisServer(6370);
            redisServer.start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (redisServer != null) {
                    redisServer.stop();
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.port", () -> 6370);
        registry.add("spring.redis.host", () -> "localhost");
    }

    @BeforeEach
    void setUp() {
        // 测试前的通用设置
    }
} 