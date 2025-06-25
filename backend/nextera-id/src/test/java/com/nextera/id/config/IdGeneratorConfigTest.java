package com.nextera.id.config;

import com.nextera.id.BaseTest;
import com.nextera.idapi.enums.IdGeneratorType;
import org.junit.jupiter.api.Test;

import jakarta.annotation.Resource;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * ID生成器配置测试
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
public class IdGeneratorConfigTest extends BaseTest {
    
    @Resource
    private IdGeneratorConfig idGeneratorConfig;
    
    @Test
    void testDefaultStrategy() {
        // 测试默认策略配置
        IdGeneratorType defaultStrategy = idGeneratorConfig.getDefaultStrategy();
        assertThat(defaultStrategy).isNotNull();
        assertThat(defaultStrategy).isEqualTo(IdGeneratorType.REDIS);
    }
    
    @Test
    void testRedisConfig() {
        // 测试Redis配置
        IdGeneratorConfig.RedisConfig redisConfig = idGeneratorConfig.getRedis();
        
        assertThat(redisConfig).isNotNull();
        assertThat(redisConfig.getHost()).isEqualTo("localhost");
        assertThat(redisConfig.getPort()).isEqualTo(6380);
        assertThat(redisConfig.getDatabase()).isEqualTo(1);
        assertThat(redisConfig.getBatchSize()).isEqualTo(10);
        assertThat(redisConfig.getExpireTime()).isEqualTo(60);
    }
    
    @Test
    void testPostgresqlConfig() {
        // 测试PostgreSQL配置
        IdGeneratorConfig.PostgresqlConfig postgresqlConfig = idGeneratorConfig.getPostgresql();
        
        assertThat(postgresqlConfig).isNotNull();
        assertThat(postgresqlConfig.getUrl()).isEqualTo("jdbc:h2:mem:testdb");
        assertThat(postgresqlConfig.getUsername()).isEqualTo("sa");
        assertThat(postgresqlConfig.getPassword()).isEmpty();
        assertThat(postgresqlConfig.getBatchSize()).isEqualTo(20);
    }
    
    @Test
    void testBusinessTypesConfig() {
        // 测试业务类型配置
        Map<String, IdGeneratorConfig.BusinessTypeConfig> businessTypes = 
            idGeneratorConfig.getBusinessTypes();
        
        assertThat(businessTypes).isNotNull();
        assertThat(businessTypes).isNotEmpty();
        
        // 测试test-user配置
        IdGeneratorConfig.BusinessTypeConfig testUserConfig = businessTypes.get("test-user");
        assertThat(testUserConfig).isNotNull();
        assertThat(testUserConfig.getStrategy()).isEqualTo(IdGeneratorType.REDIS);
        assertThat(testUserConfig.getBatchSize()).isEqualTo(10);
        assertThat(testUserConfig.getStartValue()).isEqualTo(1L);
        
        // 测试test-order配置
        IdGeneratorConfig.BusinessTypeConfig testOrderConfig = businessTypes.get("test-order");
        assertThat(testOrderConfig).isNotNull();
        assertThat(testOrderConfig.getStrategy()).isEqualTo(IdGeneratorType.POSTGRESQL);
        assertThat(testOrderConfig.getBatchSize()).isEqualTo(20);
        assertThat(testOrderConfig.getStartValue()).isEqualTo(1001L);
        
        // 测试test-article配置
        IdGeneratorConfig.BusinessTypeConfig testArticleConfig = businessTypes.get("test-article");
        assertThat(testArticleConfig).isNotNull();
        assertThat(testArticleConfig.getStrategy()).isEqualTo(IdGeneratorType.REDIS);
        assertThat(testArticleConfig.getBatchSize()).isEqualTo(5);
        assertThat(testArticleConfig.getStartValue()).isEqualTo(1L);
    }
    
    @Test
    void testGetBusinessTypeStrategy() {
        // 测试获取业务类型策略
        
        // 配置的业务类型
        IdGeneratorType userStrategy = idGeneratorConfig.getBusinessTypeStrategy("test-user");
        assertThat(userStrategy).isEqualTo(IdGeneratorType.REDIS);
        
        IdGeneratorType orderStrategy = idGeneratorConfig.getBusinessTypeStrategy("test-order");
        assertThat(orderStrategy).isEqualTo(IdGeneratorType.POSTGRESQL);
        
        // 未配置的业务类型，应该返回默认策略
        IdGeneratorType unknownStrategy = idGeneratorConfig.getBusinessTypeStrategy("unknown-type");
        assertThat(unknownStrategy).isEqualTo(IdGeneratorType.REDIS); // 默认策略
    }
    
    @Test
    void testGetBusinessTypeBatchSize() {
        // 测试获取业务类型批量大小
        
        // 配置的业务类型
        Integer userBatchSize = idGeneratorConfig.getBusinessTypeBatchSize("test-user");
        assertThat(userBatchSize).isEqualTo(10);
        
        Integer orderBatchSize = idGeneratorConfig.getBusinessTypeBatchSize("test-order");
        assertThat(orderBatchSize).isEqualTo(20);
        
        // 未配置的业务类型，应该返回默认值
        Integer unknownBatchSize = idGeneratorConfig.getBusinessTypeBatchSize("unknown-type");
        assertThat(unknownBatchSize).isEqualTo(10); // Redis默认批量大小
    }
    
    @Test
    void testGetBusinessTypeStartValue() {
        // 测试获取业务类型起始值
        
        // 配置的业务类型
        Long userStartValue = idGeneratorConfig.getBusinessTypeStartValue("test-user");
        assertThat(userStartValue).isEqualTo(1L);
        
        Long orderStartValue = idGeneratorConfig.getBusinessTypeStartValue("test-order");
        assertThat(orderStartValue).isEqualTo(1001L);
        
        // 未配置的业务类型，应该返回默认值
        Long unknownStartValue = idGeneratorConfig.getBusinessTypeStartValue("unknown-type");
        assertThat(unknownStartValue).isEqualTo(1L); // 默认起始值
    }
    
    @Test
    void testBusinessTypeConfigDefaults() {
        // 测试业务类型配置默认值
        IdGeneratorConfig.BusinessTypeConfig config = new IdGeneratorConfig.BusinessTypeConfig();
        
        assertThat(config.getStrategy()).isEqualTo(IdGeneratorType.REDIS);
        assertThat(config.getBatchSize()).isEqualTo(100);
        assertThat(config.getStartValue()).isEqualTo(1L);
    }
    
    @Test
    void testRedisConfigDefaults() {
        // 测试Redis配置默认值
        IdGeneratorConfig.RedisConfig config = new IdGeneratorConfig.RedisConfig();
        
        assertThat(config.getHost()).isEqualTo("localhost");
        assertThat(config.getPort()).isEqualTo(6379);
        assertThat(config.getDatabase()).isEqualTo(0);
        assertThat(config.getBatchSize()).isEqualTo(100);
        assertThat(config.getExpireTime()).isEqualTo(3600);
    }
    
    @Test
    void testPostgresqlConfigDefaults() {
        // 测试PostgreSQL配置默认值
        IdGeneratorConfig.PostgresqlConfig config = new IdGeneratorConfig.PostgresqlConfig();
        
        assertThat(config.getUrl()).isEqualTo("jdbc:postgresql://localhost:5432/nextera_id");
        assertThat(config.getUsername()).isEqualTo("postgres");
        assertThat(config.getPassword()).isEqualTo("123456");
        assertThat(config.getBatchSize()).isEqualTo(1000);
    }
    
    @Test
    void testConfigurationValidation() {
        // 测试配置验证
        
        // 验证必要的配置项不为空
        assertThat(idGeneratorConfig.getDefaultStrategy()).isNotNull();
        assertThat(idGeneratorConfig.getRedis()).isNotNull();
        assertThat(idGeneratorConfig.getPostgresql()).isNotNull();
        assertThat(idGeneratorConfig.getBusinessTypes()).isNotNull();
        
        // 验证Redis配置
        IdGeneratorConfig.RedisConfig redisConfig = idGeneratorConfig.getRedis();
        assertThat(redisConfig.getHost()).isNotBlank();
        assertThat(redisConfig.getPort()).isPositive();
        assertThat(redisConfig.getDatabase()).isNotNegative();
        assertThat(redisConfig.getBatchSize()).isPositive();
        assertThat(redisConfig.getExpireTime()).isPositive();
        
        // 验证PostgreSQL配置
        IdGeneratorConfig.PostgresqlConfig postgresqlConfig = idGeneratorConfig.getPostgresql();
        assertThat(postgresqlConfig.getUrl()).isNotBlank();
        assertThat(postgresqlConfig.getUsername()).isNotBlank();
        assertThat(postgresqlConfig.getBatchSize()).isPositive();
        
        // 验证业务类型配置
        Map<String, IdGeneratorConfig.BusinessTypeConfig> businessTypes = 
            idGeneratorConfig.getBusinessTypes();
        for (Map.Entry<String, IdGeneratorConfig.BusinessTypeConfig> entry : businessTypes.entrySet()) {
            String businessType = entry.getKey();
            IdGeneratorConfig.BusinessTypeConfig config = entry.getValue();
            
            assertThat(businessType).isNotBlank();
            assertThat(config.getStrategy()).isNotNull();
            assertThat(config.getBatchSize()).isPositive();
            assertThat(config.getStartValue()).isPositive();
        }
    }
    
    @Test
    void testNullBusinessType() {
        // 测试空业务类型处理
        IdGeneratorType strategy = idGeneratorConfig.getBusinessTypeStrategy(null);
        assertThat(strategy).isEqualTo(IdGeneratorType.REDIS); // 默认策略
        
        Integer batchSize = idGeneratorConfig.getBusinessTypeBatchSize(null);
        assertThat(batchSize).isEqualTo(10); // Redis默认批量大小
        
        Long startValue = idGeneratorConfig.getBusinessTypeStartValue(null);
        assertThat(startValue).isEqualTo(1L); // 默认起始值
    }
    
    @Test
    void testEmptyBusinessType() {
        // 测试空字符串业务类型处理
        IdGeneratorType strategy = idGeneratorConfig.getBusinessTypeStrategy("");
        assertThat(strategy).isEqualTo(IdGeneratorType.REDIS); // 默认策略
        
        Integer batchSize = idGeneratorConfig.getBusinessTypeBatchSize("");
        assertThat(batchSize).isEqualTo(10); // Redis默认批量大小
        
        Long startValue = idGeneratorConfig.getBusinessTypeStartValue("");
        assertThat(startValue).isEqualTo(1L); // 默认起始值
    }
    
    @Test
    void testCaseInsensitiveBusinessType() {
        // 测试业务类型大小写处理
        IdGeneratorType upperStrategy = idGeneratorConfig.getBusinessTypeStrategy("TEST-USER");
        IdGeneratorType lowerStrategy = idGeneratorConfig.getBusinessTypeStrategy("test-user");
        
        // 目前配置是大小写敏感的，未来可能需要支持大小写不敏感
        assertThat(upperStrategy).isEqualTo(IdGeneratorType.REDIS); // 默认策略（未配置）
        assertThat(lowerStrategy).isEqualTo(IdGeneratorType.REDIS); // 配置的策略
    }
} 