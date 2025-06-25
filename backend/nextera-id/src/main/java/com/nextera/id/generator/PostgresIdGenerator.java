package com.nextera.id.generator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nextera.id.config.IdGeneratorConfig;
import com.nextera.idapi.dto.IdSegment;
import com.nextera.idapi.dto.IdStatus;
import com.nextera.id.entity.IdGeneratorEntity;
import com.nextera.idapi.enums.IdGeneratorType;
import com.nextera.id.mapper.IdGeneratorMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于PostgreSQL的ID生成器
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
@Slf4j
@Component
public class PostgresIdGenerator implements IdGenerator {
    
    @Resource
    private IdGeneratorMapper idGeneratorMapper;
    
    @Resource
    private IdGeneratorConfig config;
    
    /**
     * 业务类型锁映射，确保同一业务类型的并发安全
     */
    private final ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();
    
    @Override
    public Long generateId(String businessType) {
        List<Long> ids = generateIds(businessType, 1);
        return ids.isEmpty() ? null : ids.get(0);
    }
    
    @Override
    public List<Long> generateIds(String businessType, int count) {
        if (count <= 0) {
            return Collections.emptyList();
        }
        
        IdSegment segment = generateIdSegment(businessType, count);
        List<Long> ids = new ArrayList<>();
        for (long i = segment.getStartId(); i <= segment.getEndId(); i++) {
            ids.add(i);
        }
        return ids;
    }
    
    @Override
    @Transactional
    public IdSegment generateIdSegment(String businessType, int segmentSize) {
        if (segmentSize <= 0) {
            throw new IllegalArgumentException("Segment size must be positive");
        }
        
        ReentrantLock lock = lockMap.computeIfAbsent(businessType, k -> new ReentrantLock());
        lock.lock();
        
        try {
            // 确保业务类型存在
            ensureBusinessTypeExists(businessType);
            
            // 获取当前实体
            IdGeneratorEntity entity = idGeneratorMapper.selectById(businessType);
            if (entity == null) {
                throw new RuntimeException("Business type not found: " + businessType);
            }
            
            Long currentMaxId = entity.getMaxId();
            Long newMaxId = currentMaxId + segmentSize;
            
            // 使用乐观锁更新
            int updated = idGeneratorMapper.updateMaxIdWithOptimisticLock(
                businessType, newMaxId, currentMaxId, (long) segmentSize);
            
            if (updated == 0) {
                // 乐观锁失败，重试
                log.warn("Optimistic lock failed for business type: {}, retrying...", businessType);
                return generateIdSegment(businessType, segmentSize);
            }
            
            IdSegment segment = new IdSegment();
            segment.setStartId(currentMaxId + 1);
            segment.setEndId(newMaxId);
            segment.setBusinessType(businessType);
            segment.setSegmentSize(segmentSize);
            segment.setTimestamp(System.currentTimeMillis());
            
            log.debug("Generated ID segment [{}, {}] for business type: {}", 
                     segment.getStartId(), segment.getEndId(), businessType);
            return segment;
        } catch (Exception e) {
            log.error("Failed to generate ID segment for business type: {}", businessType, e);
            throw new RuntimeException("Failed to generate ID segment", e);
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    @Transactional
    public void resetIdCounter(String businessType, Long startValue) {
        try {
            int updated = idGeneratorMapper.resetIdCounter(businessType, startValue);
            if (updated == 0) {
                // 如果没有更新任何记录，说明业务类型不存在，需要创建
                initBusinessType(businessType, startValue);
            }
            log.info("Reset ID counter for business type: {} to start value: {}", businessType, startValue);
        } catch (Exception e) {
            log.error("Failed to reset ID counter for business type: {}", businessType, e);
            throw new RuntimeException("Failed to reset ID counter", e);
        }
    }
    
    @Override
    public IdStatus getIdStatus(String businessType) {
        try {
            IdGeneratorEntity entity = idGeneratorMapper.selectById(businessType);
            
            IdStatus status = new IdStatus();
            status.setBusinessType(businessType);
            status.setGeneratorType(IdGeneratorType.POSTGRESQL);
            status.setBatchSize(config.getBusinessTypeConfig(businessType).getBatchSize());
            
            if (entity != null) {
                status.setCurrentMaxId(entity.getMaxId());
                status.setTotalGenerated(entity.getTotalGenerated());
                status.setAvailable(true);
                status.setCreateTime(entity.getCreatedAt() != null ? 
                    entity.getCreatedAt().toEpochSecond(ZoneOffset.UTC) * 1000 : null);
                status.setUpdateTime(entity.getUpdatedAt() != null ? 
                    entity.getUpdatedAt().toEpochSecond(ZoneOffset.UTC) * 1000 : null);
                status.setRemark(entity.getRemark());
            } else {
                status.setCurrentMaxId(0L);
                status.setTotalGenerated(0L);
                status.setAvailable(false);
            }
            
            return status;
        } catch (Exception e) {
            log.error("Failed to get ID status for business type: {}", businessType, e);
            throw new RuntimeException("Failed to get ID status", e);
        }
    }
    
    @Override
    public List<IdStatus> getAllIdStatus() {
        try {
            List<IdGeneratorEntity> entities = idGeneratorMapper.selectList(new QueryWrapper<>());
            List<IdStatus> statusList = new ArrayList<>();
            
            for (IdGeneratorEntity entity : entities) {
                IdStatus status = getIdStatus(entity.getBusinessType());
                statusList.add(status);
            }
            
            return statusList;
        } catch (Exception e) {
            log.error("Failed to get all ID status", e);
            throw new RuntimeException("Failed to get all ID status", e);
        }
    }
    
    @Override
    @Transactional
    public void initBusinessType(String businessType, Long startValue) {
        try {
            IdGeneratorEntity existingEntity = idGeneratorMapper.selectById(businessType);
            if (existingEntity != null) {
                log.info("Business type: {} already exists", businessType);
                return;
            }
            
            IdGeneratorEntity entity = new IdGeneratorEntity();
            entity.setBusinessType(businessType);
            entity.setMaxId(startValue - 1);
            entity.setStepSize(config.getBusinessTypeConfig(businessType).getBatchSize());
            entity.setTotalGenerated(0L);
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUpdatedAt(LocalDateTime.now());
            entity.setRemark("Auto initialized");
            
            idGeneratorMapper.insert(entity);
            log.info("Initialized business type: {} with start value: {}", businessType, startValue);
        } catch (Exception e) {
            log.error("Failed to initialize business type: {}", businessType, e);
            throw new RuntimeException("Failed to initialize business type", e);
        }
    }
    
    @Override
    @Transactional
    public void removeBusinessType(String businessType) {
        try {
            idGeneratorMapper.deleteById(businessType);
            lockMap.remove(businessType);
            log.info("Removed business type: {}", businessType);
        } catch (Exception e) {
            log.error("Failed to remove business type: {}", businessType, e);
            throw new RuntimeException("Failed to remove business type", e);
        }
    }
    
    @Override
    public boolean supports(String businessType) {
        IdGeneratorConfig.BusinessTypeConfig config = this.config.getBusinessTypeConfig(businessType);
        return config.getStrategy() == IdGeneratorType.POSTGRESQL;
    }
    
    /**
     * 确保业务类型存在
     */
    private void ensureBusinessTypeExists(String businessType) {
        IdGeneratorEntity entity = idGeneratorMapper.selectById(businessType);
        if (entity == null) {
            IdGeneratorConfig.BusinessTypeConfig businessConfig = config.getBusinessTypeConfig(businessType);
            initBusinessType(businessType, businessConfig.getStartValue());
        }
    }
} 