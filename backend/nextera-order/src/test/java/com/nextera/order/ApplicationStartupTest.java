package com.nextera.order;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 应用启动测试
 * 
 * @author Nextera Framework
 * @since 1.0.0
 */
@SpringBootTest
@ActiveProfiles("test")
public class ApplicationStartupTest {

    @Test
    public void contextLoads() {
        // 测试Spring上下文是否能正常加载
        // 如果能执行到这里说明所有Bean都能正常创建
    }
} 