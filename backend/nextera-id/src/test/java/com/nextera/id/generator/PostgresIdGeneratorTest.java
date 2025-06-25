package com.nextera.id.generator;

import com.nextera.id.BaseTest;
import com.nextera.idapi.dto.IdSegment;
import com.nextera.id.entity.IdGeneratorEntity;
import com.nextera.id.mapper.IdGeneratorMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * PostgreSQL ID生成器测试
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@Rollback
public class PostgresIdGeneratorTest extends BaseTest {
    
    @Resource
    private PostgresIdGenerator postgresIdGenerator;
    
    @Resource
    private IdGeneratorMapper idGeneratorMapper;
    
    private String testBusinessType;
    
    @BeforeEach
    void setUpPostgresTest() {
        testBusinessType = generateTestBusinessType();
        initTestBusinessType();
    }
    
    private void initTestBusinessType() {
        IdGeneratorEntity entity = new IdGeneratorEntity();
        entity.setBusinessType(testBusinessType);
        entity.setMaxId(0L);
        entity.setStepSize(20);
        entity.setTotalGenerated(0L);
        entity.setRemark("Test business type");
        idGeneratorMapper.insert(entity);
    }
    
    @Test
    void testGenerateId_Success() {
        Long id1 = postgresIdGenerator.generateId(testBusinessType);
        Long id2 = postgresIdGenerator.generateId(testBusinessType);
        
        assertThat(id1).isNotNull().isPositive();
        assertThat(id2).isNotNull().isPositive();
        assertThat(id2).isGreaterThan(id1);
    }
    
    @Test
    void testGenerateIds_Success() {
        int count = 10;
        List<Long> ids = postgresIdGenerator.generateIds(testBusinessType, count);
        
        assertThat(ids).hasSize(count);
        assertThat(ids).isSorted();
        assertThat(ids).doesNotHaveDuplicates();
        
        for (int i = 1; i < ids.size(); i++) {
            assertThat(ids.get(i)).isEqualTo(ids.get(i - 1) + 1);
        }
    }
    
    @Test
    void testGenerateIdSegment_Success() {
        int segmentSize = 100;
        IdSegment segment = postgresIdGenerator.generateIdSegment(testBusinessType, segmentSize);
        
        assertThat(segment).isNotNull();
        assertThat(segment.getBusinessType()).isEqualTo(testBusinessType);
        assertThat(segment.getSegmentSize()).isEqualTo(segmentSize);
        assertThat(segment.getEndId() - segment.getStartId() + 1).isEqualTo(segmentSize);
    }
    
    @Test
    void testSupports_Postgres() {
        String postgresBusinessType = "test-order";
        assertThat(postgresIdGenerator.supports(postgresBusinessType)).isTrue();
        
        String redisBusinessType = "test-user";
        assertThat(postgresIdGenerator.supports(redisBusinessType)).isFalse();
    }
} 