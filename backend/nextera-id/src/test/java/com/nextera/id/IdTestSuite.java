package com.nextera.id;

import com.nextera.id.config.IdGeneratorConfigTest;
import com.nextera.id.generator.PostgresIdGeneratorTest;
import com.nextera.id.generator.RedisIdGeneratorTest;
import com.nextera.id.service.IdGeneratorServiceImplTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Nextera ID生成器测试套件
 * 包含所有测试类的执行套件
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
@Suite
@SuiteDisplayName("Nextera ID Generator Test Suite")
@DisplayName("Nextera ID生成器完整测试套件")
@SelectClasses({
    // 基础组件测试
    IdGeneratorConfigTest.class,
    
    // 生成器实现测试
    RedisIdGeneratorTest.class,
    PostgresIdGeneratorTest.class,
    
    // 服务层测试
    IdGeneratorServiceImplTest.class,
    
    // 集成测试
    IdApplicationIntegrationTest.class
})
public class IdTestSuite {
    
    /**
     * 测试套件描述
     * 
     * 本测试套件包含以下测试类：
     * 
     * 1. IdGeneratorConfigTest - 配置类测试
     *    - 测试配置项的正确性
     *    - 验证默认值和业务类型配置
     *    - 检查配置验证逻辑
     * 
     * 2. RedisIdGeneratorTest - Redis ID生成器测试
     *    - 测试Redis策略的ID生成功能
     *    - 验证并发安全性
     *    - 测试错误处理和边界条件
     * 
     * 3. PostgresIdGeneratorTest - PostgreSQL ID生成器测试
     *    - 测试PostgreSQL策略的ID生成功能
     *    - 验证数据库事务和乐观锁
     *    - 测试批量操作和性能
     * 
     * 4. IdGeneratorServiceImplTest - 服务实现测试
     *    - 测试服务层的业务逻辑
     *    - 验证策略选择和参数校验
     *    - 测试多策略协同工作
     * 
     * 5. IdApplicationIntegrationTest - 集成测试
     *    - 测试完整的应用工作流程
     *    - 验证各组件间的协作
     *    - 进行性能和并发测试
     * 
     * 执行顺序：
     * - 先执行单元测试（配置、生成器、服务）
     * - 最后执行集成测试
     * 
     * 覆盖范围：
     * - 功能测试：100%覆盖核心功能
     * - 异常测试：覆盖主要异常场景
     * - 并发测试：验证高并发场景下的正确性
     * - 性能测试：基本的性能指标验证
     * - 边界测试：测试边界条件和极端情况
     */
} 