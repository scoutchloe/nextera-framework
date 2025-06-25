package com.nextera.id.service;

import com.nextera.id.config.IdGeneratorConfig;
import com.nextera.idapi.dto.IdSegment;
import com.nextera.idapi.dto.IdStatus;
import com.nextera.idapi.enums.IdGeneratorType;
import com.nextera.id.generator.IdGenerator;
import com.nextera.idapi.service.IdGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ID生成服务实现
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
@Slf4j
@Service
//@DubboService(version = "1.0.0", group = "nexteraId")
@DubboService(version = "1.0.0")
public class IdGeneratorServiceImpl implements IdGeneratorService {
    
    @Resource
    private IdGeneratorConfig config;
    
    @Resource
    private Map<String, IdGenerator> generatorMap;
    
    @Override
    public Long generateId(String businessType) {
        validateBusinessType(businessType);
        IdGenerator generator = getGenerator(businessType);
        Long id = generator.generateId(businessType);
        log.debug("Generated ID {} for business type: {}", id, businessType);
        return id;
    }
    
    @Override
    public List<Long> generateIds(String businessType, int count) {
        validateBusinessType(businessType);
        validateCount(count);
        IdGenerator generator = getGenerator(businessType);
        List<Long> ids = generator.generateIds(businessType, count);
        log.debug("Generated {} IDs for business type: {}", ids.size(), businessType);
        return ids;
    }
    
    @Override
    public IdSegment generateIdSegment(String businessType, int segmentSize) {
        validateBusinessType(businessType);
        validateSegmentSize(segmentSize);
        IdGenerator generator = getGenerator(businessType);
        IdSegment segment = generator.generateIdSegment(businessType, segmentSize);
        log.debug("Generated ID segment [{}, {}] for business type: {}", 
                 segment.getStartId(), segment.getEndId(), businessType);
        return segment;
    }
    
    @Override
    public void resetIdCounter(String businessType, Long startValue) {
        validateBusinessType(businessType);
        validateStartValue(startValue);
        IdGenerator generator = getGenerator(businessType);
        generator.resetIdCounter(businessType, startValue);
        log.info("Reset ID counter for business type: {} to start value: {}", businessType, startValue);
    }
    
    @Override
    public IdStatus getIdStatus(String businessType) {
        validateBusinessType(businessType);
        IdGenerator generator = getGenerator(businessType);
        return generator.getIdStatus(businessType);
    }
    
    @Override
    public List<IdStatus> getAllIdStatus() {
        return generatorMap.values().stream()
                .flatMap(generator -> generator.getAllIdStatus().stream())
                .collect(Collectors.toList());
    }
    
    @Override
    public void initBusinessType(String businessType, Long startValue) {
        validateBusinessType(businessType);
        if (startValue == null) {
            startValue = 1L;
        }
        validateStartValue(startValue);
        
        IdGenerator generator = getGenerator(businessType);
        generator.initBusinessType(businessType, startValue);
        log.info("Initialized business type: {} with start value: {}", businessType, startValue);
    }
    
    @Override
    public void removeBusinessType(String businessType) {
        validateBusinessType(businessType);
        IdGenerator generator = getGenerator(businessType);
        generator.removeBusinessType(businessType);
        log.info("Removed business type: {}", businessType);
    }
    
    /**
     * 获取对应的ID生成器
     */
    private IdGenerator getGenerator(String businessType) {
        IdGeneratorConfig.BusinessTypeConfig businessConfig = config.getBusinessTypeConfig(businessType);
        IdGeneratorType strategyType = businessConfig.getStrategy();
        
        for (IdGenerator generator : generatorMap.values()) {
            if (generator.supports(businessType)) {
                return generator;
            }
        }
        
        // 如果没有找到对应的生成器，使用默认策略对应的生成器
        String generatorBeanName = strategyType == IdGeneratorType.REDIS ? 
                "redisIdGenerator" : "postgresIdGenerator";
        
        IdGenerator generator = generatorMap.get(generatorBeanName);
        if (generator == null) {
            throw new RuntimeException("No suitable ID generator found for strategy: " + strategyType);
        }
        
        return generator;
    }
    
    /**
     * 验证业务类型
     */
    private void validateBusinessType(String businessType) {
        if (businessType == null || businessType.trim().isEmpty()) {
            throw new IllegalArgumentException("Business type cannot be null or empty");
        }
        if (businessType.length() > 50) {
            throw new IllegalArgumentException("Business type length cannot exceed 50 characters");
        }
    }
    
    /**
     * 验证数量
     */
    private void validateCount(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive");
        }
        if (count > 10000) {
            throw new IllegalArgumentException("Count cannot exceed 10000");
        }
    }
    
    /**
     * 验证段大小
     */
    private void validateSegmentSize(int segmentSize) {
        if (segmentSize <= 0) {
            throw new IllegalArgumentException("Segment size must be positive");
        }
        if (segmentSize > 100000) {
            throw new IllegalArgumentException("Segment size cannot exceed 100000");
        }
    }
    
    /**
     * 验证起始值
     */
    private void validateStartValue(Long startValue) {
        if (startValue == null || startValue < 1) {
            throw new IllegalArgumentException("Start value must be positive");
        }
    }
} 