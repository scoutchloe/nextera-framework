package com.nextera.id;

import com.nextera.idapi.dto.IdSegment;
import com.nextera.idapi.dto.IdStatus;
import com.nextera.idapi.enums.IdGeneratorType;
import com.nextera.id.service.IdGeneratorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

/**
 * ID生成器应用集成测试
 * 测试整个应用的完整功能流程
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
@SpringBootTest(classes = IdApplication.class)
public class IdApplicationIntegrationTest extends BaseTest {
    
    @Resource
    private IdGeneratorServiceImpl idGeneratorService;
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    private List<String> testBusinessTypes;
    
    @BeforeEach
    void setUpIntegrationTest() {
        testBusinessTypes = new ArrayList<>();
        // 清理测试数据
        cleanAllTestData();
    }
    
    private void cleanAllTestData() {
        // 清理Redis中的测试数据
        for (String businessType : testBusinessTypes) {
            redisTemplate.delete("nextera:id:" + businessType);
            redisTemplate.delete("nextera:id:info:" + businessType);
        }
        testBusinessTypes.clear();
    }
    
    private String createTestBusinessType() {
        String businessType = generateTestBusinessType();
        testBusinessTypes.add(businessType);
        return businessType;
    }
    
    @Test
    void testCompleteWorkflow_RedisStrategy() {
        // 完整的Redis策略工作流程测试
        String businessType = "test-user"; // Redis策略
        
        // 1. 初始状态检查
        IdStatus initialStatus = idGeneratorService.getIdStatus(businessType);
        assertThat(initialStatus.getGeneratorType()).isEqualTo(IdGeneratorType.REDIS);
        
        // 2. 生成单个ID
        Long id1 = idGeneratorService.generateId(businessType);
        assertThat(id1).isNotNull().isPositive();
        
        // 3. 生成批量ID
        List<Long> batchIds = idGeneratorService.generateIds(businessType, 10);
        assertThat(batchIds).hasSize(10);
        assertThat(batchIds.get(0)).isEqualTo(id1 + 1);
        
        // 4. 生成ID段
        IdSegment segment = idGeneratorService.generateIdSegment(businessType, 50);
        assertThat(segment).isNotNull();
        assertThat(segment.getStartId()).isEqualTo(batchIds.get(9) + 1);
        
        // 5. 检查状态
        IdStatus finalStatus = idGeneratorService.getIdStatus(businessType);
        assertThat(finalStatus.getAvailable()).isTrue();
        assertThat(finalStatus.getCurrentMaxId()).isEqualTo(segment.getEndId());
        
        // 6. 重置计数器
        Long resetValue = 2000L;
        idGeneratorService.resetIdCounter(businessType, resetValue);
        Long newId = idGeneratorService.generateId(businessType);
        assertThat(newId).isEqualTo(resetValue);
        
        // 7. 删除业务类型
        idGeneratorService.removeBusinessType(businessType);
        IdStatus removedStatus = idGeneratorService.getIdStatus(businessType);
        assertThat(removedStatus.getAvailable()).isFalse();
    }
    
    @Test
    void testCompleteWorkflow_PostgresStrategy() {
        // 完整的PostgreSQL策略工作流程测试
        String businessType = "test-order"; // PostgreSQL策略
        
        // 1. 初始状态检查
        IdStatus initialStatus = idGeneratorService.getIdStatus(businessType);
        assertThat(initialStatus.getGeneratorType()).isEqualTo(IdGeneratorType.POSTGRESQL);
        
        // 2. 生成单个ID
        Long id1 = idGeneratorService.generateId(businessType);
        assertThat(id1).isNotNull().isPositive();
        
        // 3. 生成批量ID
        List<Long> batchIds = idGeneratorService.generateIds(businessType, 15);
        assertThat(batchIds).hasSize(15);
        assertThat(batchIds.get(0)).isEqualTo(id1 + 1);
        
        // 4. 生成ID段
        IdSegment segment = idGeneratorService.generateIdSegment(businessType, 30);
        assertThat(segment).isNotNull();
        assertThat(segment.getStartId()).isEqualTo(batchIds.get(14) + 1);
        
        // 5. 检查状态
        IdStatus finalStatus = idGeneratorService.getIdStatus(businessType);
        assertThat(finalStatus.getAvailable()).isTrue();
        assertThat(finalStatus.getCurrentMaxId()).isEqualTo(segment.getEndId());
        
        // 6. 重置计数器
        Long resetValue = 3000L;
        idGeneratorService.resetIdCounter(businessType, resetValue);
        Long newId = idGeneratorService.generateId(businessType);
        assertThat(newId).isEqualTo(resetValue);
    }
    
    @Test
    void testMultipleBusinessTypes_Integration() {
        // 多业务类型集成测试
        String redisBusinessType1 = createTestBusinessType();
        String redisBusinessType2 = createTestBusinessType();
        String postgresBusinessType = "test-order";
        
        // 初始化不同策略的业务类型
        idGeneratorService.initBusinessType(redisBusinessType1, 100L);
        idGeneratorService.initBusinessType(redisBusinessType2, 200L);
        idGeneratorService.initBusinessType(postgresBusinessType, 1000L);
        
        // 并发生成ID
        List<Long> ids1 = idGeneratorService.generateIds(redisBusinessType1, 10);
        List<Long> ids2 = idGeneratorService.generateIds(redisBusinessType2, 10);
        List<Long> ids3 = idGeneratorService.generateIds(postgresBusinessType, 10);
        
        // 验证各自独立
        assertThat(ids1.get(0)).isEqualTo(100L);
        assertThat(ids2.get(0)).isEqualTo(200L);
        assertThat(ids3.get(0)).isEqualTo(1000L);
        
        // 验证连续性
        for (int i = 1; i < 10; i++) {
            assertThat(ids1.get(i)).isEqualTo(ids1.get(i - 1) + 1);
            assertThat(ids2.get(i)).isEqualTo(ids2.get(i - 1) + 1);
            assertThat(ids3.get(i)).isEqualTo(ids3.get(i - 1) + 1);
        }
        
        // 验证状态
        IdStatus status1 = idGeneratorService.getIdStatus(redisBusinessType1);
        IdStatus status2 = idGeneratorService.getIdStatus(redisBusinessType2);
        IdStatus status3 = idGeneratorService.getIdStatus(postgresBusinessType);
        
        assertThat(status1.getGeneratorType()).isEqualTo(IdGeneratorType.REDIS);
        assertThat(status2.getGeneratorType()).isEqualTo(IdGeneratorType.REDIS);
        assertThat(status3.getGeneratorType()).isEqualTo(IdGeneratorType.POSTGRESQL);
    }
    
    @Test
    void testHighConcurrency_Integration() throws Exception {
        // 高并发集成测试
        String businessType = createTestBusinessType();
        int threadCount = 20;
        int idsPerThread = 50;
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        
        List<Future<List<Long>>> futures = new ArrayList<>();
        
        // 创建并发任务
        for (int i = 0; i < threadCount; i++) {
            Future<List<Long>> future = executor.submit(() -> {
                try {
                    startLatch.await(); // 等待统一开始
                    return idGeneratorService.generateIds(businessType, idsPerThread);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return Collections.emptyList();
                } finally {
                    endLatch.countDown();
                }
            });
            futures.add(future);
        }
        
        try {
            // 开始执行
            startLatch.countDown();
            
            // 等待所有任务完成
            boolean finished = endLatch.await(30, TimeUnit.SECONDS);
            assertThat(finished).isTrue();
            
            // 收集所有ID
            java.util.Set<Long> allIds = new java.util.HashSet<>();
            for (Future<List<Long>> future : futures) {
                List<Long> ids = future.get();
                assertThat(ids).hasSize(idsPerThread);
                allIds.addAll(ids);
            }
            
            // 验证ID唯一性
            assertThat(allIds).hasSize(threadCount * idsPerThread);
            
            // 验证最终状态
            IdStatus finalStatus = idGeneratorService.getIdStatus(businessType);
            assertThat(finalStatus.getAvailable()).isTrue();
            assertThat(finalStatus.getCurrentMaxId()).isEqualTo(threadCount * idsPerThread);
            
        } finally {
            executor.shutdown();
        }
    }
    
    @Test
    void testIdSegmentWorkflow() {
        // ID段工作流程测试
        String businessType = createTestBusinessType();
        
        // 生成多个ID段
        IdSegment segment1 = idGeneratorService.generateIdSegment(businessType, 100);
        IdSegment segment2 = idGeneratorService.generateIdSegment(businessType, 200);
        IdSegment segment3 = idGeneratorService.generateIdSegment(businessType, 150);
        
        // 验证ID段连续性
        assertThat(segment2.getStartId()).isEqualTo(segment1.getEndId() + 1);
        assertThat(segment3.getStartId()).isEqualTo(segment2.getEndId() + 1);
        
        // 验证ID段功能
        assertThat(segment1.contains(segment1.getStartId())).isTrue();
        assertThat(segment1.contains(segment1.getEndId())).isTrue();
        assertThat(segment1.contains(segment1.getStartId() - 1)).isFalse();
        assertThat(segment1.contains(segment1.getEndId() + 1)).isFalse();
        
        // 验证剩余数量计算
        Long midId = segment1.getStartId() + 50;
        assertThat(segment1.remainingCount(midId)).isEqualTo(segment1.getEndId() - midId);
        assertThat(segment1.remainingCount(segment1.getEndId())).isEqualTo(0);
        assertThat(segment1.remainingCount(segment1.getEndId() + 1)).isEqualTo(0);
    }
    
    @Test
    void testMixedOperations_Integration() {
        // 混合操作集成测试
        String businessType = createTestBusinessType();
        
        // 混合不同类型的操作
        Long singleId1 = idGeneratorService.generateId(businessType);
        List<Long> batchIds1 = idGeneratorService.generateIds(businessType, 5);
        IdSegment segment1 = idGeneratorService.generateIdSegment(businessType, 20);
        Long singleId2 = idGeneratorService.generateId(businessType);
        List<Long> batchIds2 = idGeneratorService.generateIds(businessType, 8);
        IdSegment segment2 = idGeneratorService.generateIdSegment(businessType, 15);
        
        // 验证操作顺序
        assertThat(batchIds1.get(0)).isEqualTo(singleId1 + 1);
        assertThat(segment1.getStartId()).isEqualTo(batchIds1.get(4) + 1);
        assertThat(singleId2).isEqualTo(segment1.getEndId() + 1);
        assertThat(batchIds2.get(0)).isEqualTo(singleId2 + 1);
        assertThat(segment2.getStartId()).isEqualTo(batchIds2.get(7) + 1);
        
        // 验证所有ID唯一性
        java.util.Set<Long> allIds = new java.util.HashSet<>();
        allIds.add(singleId1);
        allIds.addAll(batchIds1);
        for (long id = segment1.getStartId(); id <= segment1.getEndId(); id++) {
            allIds.add(id);
        }
        allIds.add(singleId2);
        allIds.addAll(batchIds2);
        for (long id = segment2.getStartId(); id <= segment2.getEndId(); id++) {
            allIds.add(id);
        }
        
        int expectedSize = 1 + 5 + 20 + 1 + 8 + 15; // 50个ID
        assertThat(allIds).hasSize(expectedSize);
    }
    
    @Test
    void testErrorHandling_Integration() {
        // 错误处理集成测试
        
        // 测试空参数
        assertThatThrownBy(() -> idGeneratorService.generateId(null))
                .isInstanceOf(IllegalArgumentException.class);
        
        assertThatThrownBy(() -> idGeneratorService.generateId(""))
                .isInstanceOf(IllegalArgumentException.class);
        
        assertThatThrownBy(() -> idGeneratorService.generateIds(null, 5))
                .isInstanceOf(IllegalArgumentException.class);
        
        assertThatThrownBy(() -> idGeneratorService.generateIdSegment(null, 100))
                .isInstanceOf(IllegalArgumentException.class);
        
        // 测试无效参数
        String businessType = createTestBusinessType();
        
        assertThatThrownBy(() -> idGeneratorService.generateIdSegment(businessType, 0))
                .isInstanceOf(IllegalArgumentException.class);
        
        assertThatThrownBy(() -> idGeneratorService.generateIdSegment(businessType, -1))
                .isInstanceOf(IllegalArgumentException.class);
        
        assertThatThrownBy(() -> idGeneratorService.resetIdCounter(businessType, null))
                .isInstanceOf(IllegalArgumentException.class);
        
        // 验证错误后系统仍然可用
        Long validId = idGeneratorService.generateId(businessType);
        assertThat(validId).isNotNull().isPositive();
    }
    
    @Test
    void testPerformance_Integration() {
        // 性能集成测试
        String businessType = createTestBusinessType();
        
        // 预热
        idGeneratorService.generateIds(businessType, 100);
        
        // 测试单个ID生成性能
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            idGeneratorService.generateId(businessType);
        }
        long singleIdTime = System.currentTimeMillis() - startTime;
        
        // 测试批量ID生成性能
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            idGeneratorService.generateIds(businessType, 10);
        }
        long batchIdTime = System.currentTimeMillis() - startTime;
        
        // 测试ID段生成性能
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 50; i++) {
            idGeneratorService.generateIdSegment(businessType, 20);
        }
        long segmentIdTime = System.currentTimeMillis() - startTime;
        
        // 记录性能数据（实际项目中可以使用专业的性能测试工具）
        System.out.println("Single ID generation time: " + singleIdTime + "ms");
        System.out.println("Batch ID generation time: " + batchIdTime + "ms");
        System.out.println("Segment ID generation time: " + segmentIdTime + "ms");
        
        // 基本性能断言（根据实际环境调整）
        assertThat(singleIdTime).isLessThan(5000); // 5秒内完成1000次单个ID生成
        assertThat(batchIdTime).isLessThan(3000);   // 3秒内完成1000次批量ID生成
        assertThat(segmentIdTime).isLessThan(2000); // 2秒内完成1000次ID段生成
    }
} 