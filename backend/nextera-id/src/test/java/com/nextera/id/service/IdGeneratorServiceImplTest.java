package com.nextera.id.service;

import com.nextera.id.BaseTest;
import com.nextera.idapi.dto.IdSegment;
import com.nextera.idapi.dto.IdStatus;
import com.nextera.idapi.enums.IdGeneratorType;
import com.nextera.id.generator.RedisIdGenerator;
import com.nextera.id.generator.PostgresIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.*;

/**
 * ID生成器服务实现测试
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
public class IdGeneratorServiceImplTest extends BaseTest {
    
    @Resource
    private IdGeneratorServiceImpl idGeneratorService;
    
    @Resource
    private RedisIdGenerator redisIdGenerator;
    
    @Resource
    private PostgresIdGenerator postgresIdGenerator;
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    private String redisBusinessType;
    private String postgresBusinessType;
    private String defaultBusinessType;
    
    @BeforeEach
    void setUpServiceTest() {
        redisBusinessType = "test-user";      // 配置为Redis策略
        postgresBusinessType = "test-order";  // 配置为PostgreSQL策略
        defaultBusinessType = generateTestBusinessType(); // 使用默认策略
        
        cleanTestData();
    }
    
    private void cleanTestData() {
        // 清理Redis测试数据
        redisTemplate.delete("nextera:id:" + redisBusinessType);
        redisTemplate.delete("nextera:id:info:" + redisBusinessType);
        redisTemplate.delete("nextera:id:" + defaultBusinessType);
        redisTemplate.delete("nextera:id:info:" + defaultBusinessType);
    }
    
    @Test
    void testGenerateId_RedisStrategy() {
        // 测试Redis策略生成ID
        Long id1 = idGeneratorService.generateId(redisBusinessType);
        Long id2 = idGeneratorService.generateId(redisBusinessType);
        
        assertThat(id1).isNotNull().isPositive();
        assertThat(id2).isNotNull().isPositive();
        assertThat(id2).isGreaterThan(id1);
    }
    
    @Test
    void testGenerateId_PostgresStrategy() {
        // 测试PostgreSQL策略生成ID
        Long id1 = idGeneratorService.generateId(postgresBusinessType);
        Long id2 = idGeneratorService.generateId(postgresBusinessType);

        System.out.println("id1: " + id1);
        System.out.println("id2: " + id2);
        assertThat(id1).isNotNull().isPositive();
        assertThat(id2).isNotNull().isPositive();
        assertThat(id2).isGreaterThan(id1);
    }
    
    @Test
    void testGenerateId_DefaultStrategy() {
        // 测试默认策略生成ID
        Long id1 = idGeneratorService.generateId(defaultBusinessType);
        Long id2 = idGeneratorService.generateId(defaultBusinessType);
        
        assertThat(id1).isNotNull().isPositive();
        assertThat(id2).isNotNull().isPositive();
        assertThat(id2).isGreaterThan(id1);
    }
    
    @Test
    void testGenerateId_NullBusinessType() {
        // 测试空业务类型
        assertThatThrownBy(() -> idGeneratorService.generateId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Business type cannot be null or empty");
    }
    
    @Test
    void testGenerateId_EmptyBusinessType() {
        // 测试空字符串业务类型
        assertThatThrownBy(() -> idGeneratorService.generateId(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Business type cannot be null or empty");
    }
    
    @Test
    void testGenerateIds_Success() {
        // 测试批量生成ID
        int count = 10;
        List<Long> ids = idGeneratorService.generateIds(redisBusinessType, count);
        
        assertThat(ids).hasSize(count);
        assertThat(ids).isSorted();
        assertThat(ids).doesNotHaveDuplicates();
        
        // 验证ID连续性
        for (int i = 1; i < ids.size(); i++) {
            assertThat(ids.get(i)).isEqualTo(ids.get(i - 1) + 1);
        }
    }
    
    @Test
    void testGenerateIds_EmptyCount() {
        // 测试生成0个ID
        List<Long> ids = idGeneratorService.generateIds(redisBusinessType, 0);
        assertThat(ids).isEmpty();
    }
    
    @Test
    void testGenerateIds_NegativeCount() {
        // 测试负数数量
        List<Long> ids = idGeneratorService.generateIds(redisBusinessType, -1);
        assertThat(ids).isEmpty();
    }
    
    @Test
    void testGenerateIds_NullBusinessType() {
        // 测试空业务类型
        assertThatThrownBy(() -> idGeneratorService.generateIds(null, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Business type cannot be null or empty");
    }
    
    @Test
    void testGenerateIdSegment_Success() {
        // 测试生成ID段
        int segmentSize = 100;
        IdSegment segment = idGeneratorService.generateIdSegment(redisBusinessType, segmentSize);
        
        assertThat(segment).isNotNull();
        assertThat(segment.getBusinessType()).isEqualTo(redisBusinessType);
        assertThat(segment.getSegmentSize()).isEqualTo(segmentSize);
        assertThat(segment.getEndId() - segment.getStartId() + 1).isEqualTo(segmentSize);
        assertThat(segment.getTimestamp()).isPositive();
    }
    
    @Test
    void testGenerateIdSegment_InvalidSize() {
        // 测试无效的段大小
        assertThatThrownBy(() -> idGeneratorService.generateIdSegment(redisBusinessType, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Segment size must be positive");
        
        assertThatThrownBy(() -> idGeneratorService.generateIdSegment(redisBusinessType, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Segment size must be positive");
    }
    
    @Test
    void testGenerateIdSegment_NullBusinessType() {
        // 测试空业务类型
        assertThatThrownBy(() -> idGeneratorService.generateIdSegment(null, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Business type cannot be null or empty");
    }
    
    @Test
    void testGetIdStatus_RedisStrategy() {
        // 先生成一些ID
        idGeneratorService.generateId(redisBusinessType);
        idGeneratorService.generateIds(redisBusinessType, 5);
        
        IdStatus status = idGeneratorService.getIdStatus(redisBusinessType);
        
        assertThat(status).isNotNull();
        assertThat(status.getBusinessType()).isEqualTo(redisBusinessType);
        assertThat(status.getGeneratorType()).isEqualTo(IdGeneratorType.REDIS);
        assertThat(status.getAvailable()).isTrue();
        assertThat(status.getCurrentMaxId()).isPositive();
    }
    
    @Test
    void testGetIdStatus_PostgresStrategy() {
        // 先生成一些ID
        idGeneratorService.generateId(postgresBusinessType);
        
        IdStatus status = idGeneratorService.getIdStatus(postgresBusinessType);
        
        assertThat(status).isNotNull();
        assertThat(status.getBusinessType()).isEqualTo(postgresBusinessType);
        assertThat(status.getGeneratorType()).isEqualTo(IdGeneratorType.POSTGRESQL);
        assertThat(status.getAvailable()).isTrue();
        assertThat(status.getCurrentMaxId()).isPositive();
    }
    
    @Test
    void testGetIdStatus_NullBusinessType() {
        // 测试空业务类型
        assertThatThrownBy(() -> idGeneratorService.getIdStatus(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Business type cannot be null or empty");
    }
    
    @Test
    void testResetIdCounter_Success() {
        // 先生成一些ID
        idGeneratorService.generateId(redisBusinessType);
        idGeneratorService.generateId(redisBusinessType);
        
        // 重置计数器
        Long startValue = 1000L;
        idGeneratorService.resetIdCounter(redisBusinessType, startValue);
        
        // 验证重置后的ID
        Long newId = idGeneratorService.generateId(redisBusinessType);
        assertThat(newId).isEqualTo(startValue);
    }
    
    @Test
    void testResetIdCounter_NullBusinessType() {
        // 测试空业务类型
        assertThatThrownBy(() -> idGeneratorService.resetIdCounter(null, 1000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Business type cannot be null or empty");
    }
    
    @Test
    void testResetIdCounter_NullStartValue() {
        // 测试空起始值
        assertThatThrownBy(() -> idGeneratorService.resetIdCounter(redisBusinessType, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Start value cannot be null");
    }
    
    @Test
    void testInitBusinessType_Success() {
        // 测试初始化业务类型
        String newBusinessType = generateTestBusinessType();
        Long startValue = 500L;
        
        idGeneratorService.initBusinessType(newBusinessType, startValue);
        
        // 验证初始化
        Long id = idGeneratorService.generateId(newBusinessType);
        assertThat(id).isEqualTo(startValue);
    }
    
    @Test
    void testInitBusinessType_NullBusinessType() {
        // 测试空业务类型
        assertThatThrownBy(() -> idGeneratorService.initBusinessType(null, 1000L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Business type cannot be null or empty");
    }
    
    @Test
    void testRemoveBusinessType_Success() {
        // 先生成一些ID
        idGeneratorService.generateId(redisBusinessType);
        
        // 验证存在
        IdStatus statusBefore = idGeneratorService.getIdStatus(redisBusinessType);
        assertThat(statusBefore.getAvailable()).isTrue();
        
        // 删除业务类型
        idGeneratorService.removeBusinessType(redisBusinessType);
        
        // 验证删除
        IdStatus statusAfter = idGeneratorService.getIdStatus(redisBusinessType);
        assertThat(statusAfter.getAvailable()).isFalse();
    }
    
    @Test
    void testRemoveBusinessType_NullBusinessType() {
        // 测试空业务类型
        assertThatThrownBy(() -> idGeneratorService.removeBusinessType(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Business type cannot be null or empty");
    }
    
    @Test
    void testConcurrentIdGeneration_MultipleStrategies() throws Exception {
        // 并发测试多种策略
        int threadCount = 4;
        int idsPerThread = 25;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        try {
            // 创建并发任务
            CompletableFuture<List<Long>> redisTask1 = CompletableFuture.supplyAsync(
                () -> idGeneratorService.generateIds(redisBusinessType, idsPerThread), executor);
            
            CompletableFuture<List<Long>> redisTask2 = CompletableFuture.supplyAsync(
                () -> idGeneratorService.generateIds(redisBusinessType, idsPerThread), executor);
            
            CompletableFuture<List<Long>> postgresTask1 = CompletableFuture.supplyAsync(
                () -> idGeneratorService.generateIds(postgresBusinessType, idsPerThread), executor);
            
            CompletableFuture<List<Long>> postgresTask2 = CompletableFuture.supplyAsync(
                () -> idGeneratorService.generateIds(postgresBusinessType, idsPerThread), executor);
            
            // 等待所有任务完成
            CompletableFuture<Void> allTasks = CompletableFuture.allOf(
                redisTask1, redisTask2, postgresTask1, postgresTask2);
            allTasks.get();
            
            // 验证Redis ID唯一性
            java.util.Set<Long> redisIds = new java.util.HashSet<>();
            redisIds.addAll(redisTask1.get());
            redisIds.addAll(redisTask2.get());
            assertThat(redisIds).hasSize(2 * idsPerThread);
            
            // 验证PostgreSQL ID唯一性
            java.util.Set<Long> postgresIds = new java.util.HashSet<>();
            postgresIds.addAll(postgresTask1.get());
            postgresIds.addAll(postgresTask2.get());
            assertThat(postgresIds).hasSize(2 * idsPerThread);
            
        } finally {
            executor.shutdown();
        }
    }
    
    @Test
    void testStrategySelection() {
        // 测试策略选择逻辑
        
        // Redis策略业务类型
        IdStatus redisStatus = idGeneratorService.getIdStatus(redisBusinessType);
        Long redisId = idGeneratorService.generateId(redisBusinessType);
        IdStatus redisStatusAfter = idGeneratorService.getIdStatus(redisBusinessType);
        
        assertThat(redisStatusAfter.getGeneratorType()).isEqualTo(IdGeneratorType.REDIS);
        
        // PostgreSQL策略业务类型
        IdStatus postgresStatus = idGeneratorService.getIdStatus(postgresBusinessType);
        Long postgresId = idGeneratorService.generateId(postgresBusinessType);
        IdStatus postgresStatusAfter = idGeneratorService.getIdStatus(postgresBusinessType);
        
        assertThat(postgresStatusAfter.getGeneratorType()).isEqualTo(IdGeneratorType.POSTGRESQL);
        
        // 验证ID独立性
        assertThat(redisId).isPositive();
        assertThat(postgresId).isPositive();
    }
    
    @Test
    void testMultipleBusinessTypes() {
        // 测试多个业务类型
        String businessType1 = generateTestBusinessType();
        String businessType2 = generateTestBusinessType();
        String businessType3 = generateTestBusinessType();
        
        try {
            // 为不同业务类型生成ID
            Long id1 = idGeneratorService.generateId(businessType1);
            Long id2 = idGeneratorService.generateId(businessType2);
            Long id3 = idGeneratorService.generateId(businessType3);
            
            assertThat(id1).isPositive();
            assertThat(id2).isPositive();
            assertThat(id3).isPositive();
            
            // 验证各自独立
            Long nextId1 = idGeneratorService.generateId(businessType1);
            Long nextId2 = idGeneratorService.generateId(businessType2);
            Long nextId3 = idGeneratorService.generateId(businessType3);
            
            assertThat(nextId1).isEqualTo(id1 + 1);
            assertThat(nextId2).isEqualTo(id2 + 1);
            assertThat(nextId3).isEqualTo(id3 + 1);
            
        } finally {
            // 清理测试数据
            idGeneratorService.removeBusinessType(businessType1);
            idGeneratorService.removeBusinessType(businessType2);
            idGeneratorService.removeBusinessType(businessType3);
        }
    }
} 