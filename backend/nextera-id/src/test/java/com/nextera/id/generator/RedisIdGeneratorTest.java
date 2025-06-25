package com.nextera.id.generator;

import com.nextera.id.BaseTest;
import com.nextera.idapi.dto.IdSegment;
import com.nextera.idapi.dto.IdStatus;
import com.nextera.idapi.enums.IdGeneratorType;
import org.junit.jupiter.api.AfterEach;
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
 * Redis ID生成器测试
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
public class RedisIdGeneratorTest extends BaseTest {
    
    @Resource
    private RedisIdGenerator redisIdGenerator;
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    private String testBusinessType;
    
    @BeforeEach
    void setUpRedisTest() {
        testBusinessType = generateTestBusinessType();
        // 清理Redis测试数据
        cleanRedisTestData();
    }
    
    @AfterEach
    void tearDown() {
        // 清理Redis测试数据
        cleanRedisTestData();
    }
    
    private void cleanRedisTestData() {
        redisTemplate.delete("nextera:id:" + testBusinessType);
        redisTemplate.delete("nextera:id:info:" + testBusinessType);
    }
    
    @Test
    void testGenerateId_Success() {
        // 测试生成单个ID
        Long id1 = redisIdGenerator.generateId(testBusinessType);
        Long id2 = redisIdGenerator.generateId(testBusinessType);
        
        assertThat(id1).isNotNull().isPositive();
        assertThat(id2).isNotNull().isPositive();
        assertThat(id2).isGreaterThan(id1);
    }
    
    @Test
    void testGenerateIds_Success() {
        // 测试批量生成ID
        int count = 10;
        List<Long> ids = redisIdGenerator.generateIds(testBusinessType, count);
        
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
        List<Long> ids = redisIdGenerator.generateIds(testBusinessType, 0);
        assertThat(ids).isEmpty();
    }
    
    @Test
    void testGenerateIds_NegativeCount() {
        // 测试负数数量
        List<Long> ids = redisIdGenerator.generateIds(testBusinessType, -1);
        assertThat(ids).isEmpty();
    }
    
    @Test
    void testGenerateIdSegment_Success() {
        // 测试生成ID段
        int segmentSize = 100;
        IdSegment segment = redisIdGenerator.generateIdSegment(testBusinessType, segmentSize);
        
        assertThat(segment).isNotNull();
        assertThat(segment.getBusinessType()).isEqualTo(testBusinessType);
        assertThat(segment.getSegmentSize()).isEqualTo(segmentSize);
        assertThat(segment.getEndId() - segment.getStartId() + 1).isEqualTo(segmentSize);
        assertThat(segment.getTimestamp()).isPositive();
        
        // 验证ID段方法
        assertThat(segment.contains(segment.getStartId())).isTrue();
        assertThat(segment.contains(segment.getEndId())).isTrue();
        assertThat(segment.contains(segment.getStartId() - 1)).isFalse();
        assertThat(segment.contains(segment.getEndId() + 1)).isFalse();
    }
    
    @Test
    void testGenerateIdSegment_InvalidSize() {
        // 测试无效的段大小
        assertThatThrownBy(() -> redisIdGenerator.generateIdSegment(testBusinessType, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Segment size must be positive");
        
        assertThatThrownBy(() -> redisIdGenerator.generateIdSegment(testBusinessType, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Segment size must be positive");
    }
    
    @Test
    void testResetIdCounter_Success() {
        // 先生成一些ID
        redisIdGenerator.generateId(testBusinessType);
        redisIdGenerator.generateId(testBusinessType);
        
        // 重置计数器
        Long startValue = 1000L;
        redisIdGenerator.resetIdCounter(testBusinessType, startValue);
        
        // 验证重置后的ID
        Long newId = redisIdGenerator.generateId(testBusinessType);
        assertThat(newId).isEqualTo(startValue);
    }
    
    @Test
    void testGetIdStatus_NewBusinessType() {
        // 测试新业务类型的状态
        IdStatus status = redisIdGenerator.getIdStatus(testBusinessType);
        
        assertThat(status).isNotNull();
        assertThat(status.getBusinessType()).isEqualTo(testBusinessType);
        assertThat(status.getGeneratorType()).isEqualTo(IdGeneratorType.REDIS);
        assertThat(status.getAvailable()).isFalse();
    }
    
    @Test
    void testGetIdStatus_ExistingBusinessType() {
        // 先生成一些ID
        redisIdGenerator.generateId(testBusinessType);
        redisIdGenerator.generateIds(testBusinessType, 5);
        
        IdStatus status = redisIdGenerator.getIdStatus(testBusinessType);
        
        assertThat(status).isNotNull();
        assertThat(status.getBusinessType()).isEqualTo(testBusinessType);
        assertThat(status.getGeneratorType()).isEqualTo(IdGeneratorType.REDIS);
        assertThat(status.getAvailable()).isTrue();
        assertThat(status.getCurrentMaxId()).isPositive();
    }
    
    @Test
    void testInitBusinessType_Success() {
        // 测试初始化业务类型
        Long startValue = 500L;
        redisIdGenerator.initBusinessType(testBusinessType, startValue);
        
        // 验证初始化
        Long id = redisIdGenerator.generateId(testBusinessType);
        assertThat(id).isEqualTo(startValue);
    }
    
    @Test
    void testRemoveBusinessType_Success() {
        // 先生成一些ID
        redisIdGenerator.generateId(testBusinessType);
        
        // 验证存在
        IdStatus statusBefore = redisIdGenerator.getIdStatus(testBusinessType);
        assertThat(statusBefore.getAvailable()).isTrue();
        
        // 删除业务类型
        redisIdGenerator.removeBusinessType(testBusinessType);
        
        // 验证删除
        IdStatus statusAfter = redisIdGenerator.getIdStatus(testBusinessType);
        assertThat(statusAfter.getAvailable()).isFalse();
    }
    
    @Test
    void testSupports_Redis() {
        // 配置Redis策略的业务类型
        String redisBusinessType = "test-user";
        assertThat(redisIdGenerator.supports(redisBusinessType)).isTrue();
        
        // 配置PostgreSQL策略的业务类型
        String postgresBusinessType = "test-order";
        assertThat(redisIdGenerator.supports(postgresBusinessType)).isFalse();
    }
    
    @Test
    void testConcurrentIdGeneration() throws Exception {
        // 并发测试
        int threadCount = 10;
        int idsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        
        try {
            CompletableFuture<List<Long>>[] futures = new CompletableFuture[threadCount];
            
            for (int i = 0; i < threadCount; i++) {
                futures[i] = CompletableFuture.supplyAsync(() -> 
                    redisIdGenerator.generateIds(testBusinessType, idsPerThread), executor);
            }
            
            // 等待所有任务完成
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures);
            allFutures.get();
            
            // 收集所有ID
            java.util.Set<Long> allIds = new java.util.HashSet<>();
            for (CompletableFuture<List<Long>> future : futures) {
                List<Long> ids = future.get();
                assertThat(ids).hasSize(idsPerThread);
                allIds.addAll(ids);
            }
            
            // 验证ID唯一性
            assertThat(allIds).hasSize(threadCount * idsPerThread);
            
        } finally {
            executor.shutdown();
        }
    }
    
    @Test
    void testIdSegmentRemaining() {
        // 测试ID段剩余数量计算
        IdSegment segment = redisIdGenerator.generateIdSegment(testBusinessType, 100);
        
        // 测试剩余数量计算
        assertThat(segment.remainingCount(null)).isEqualTo(100);
        assertThat(segment.remainingCount(segment.getStartId())).isEqualTo(segment.getEndId() - segment.getStartId());
        assertThat(segment.remainingCount(segment.getEndId())).isEqualTo(0);
        assertThat(segment.remainingCount(segment.getEndId() + 1)).isEqualTo(0);
        assertThat(segment.remainingCount(segment.getStartId() - 1)).isEqualTo(100);
    }
    
    @Test
    void testMultipleBusinessTypes() {
        // 测试多个业务类型
        String businessType1 = generateTestBusinessType();
        String businessType2 = generateTestBusinessType();
        
        try {
            // 为不同业务类型生成ID
            Long id1 = redisIdGenerator.generateId(businessType1);
            Long id2 = redisIdGenerator.generateId(businessType2);
            
            assertThat(id1).isPositive();
            assertThat(id2).isPositive();
            
            // 验证各自独立
            Long nextId1 = redisIdGenerator.generateId(businessType1);
            Long nextId2 = redisIdGenerator.generateId(businessType2);
            
            assertThat(nextId1).isEqualTo(id1 + 1);
            assertThat(nextId2).isEqualTo(id2 + 1);
            
        } finally {
            // 清理测试数据
            redisIdGenerator.removeBusinessType(businessType1);
            redisIdGenerator.removeBusinessType(businessType2);
        }
    }
} 