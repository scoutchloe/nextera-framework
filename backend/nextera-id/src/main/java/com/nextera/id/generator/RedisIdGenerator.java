package com.nextera.id.generator;

import com.nextera.id.config.IdGeneratorConfig;
import com.nextera.idapi.dto.IdSegment;
import com.nextera.idapi.dto.IdStatus;
import com.nextera.idapi.enums.IdGeneratorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 基于Redis的ID生成器
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
@Slf4j
@Component
public class RedisIdGenerator implements IdGenerator {
    
    private static final String ID_KEY_PREFIX = "nextera:id:";
    private static final String ID_INFO_KEY_PREFIX = "nextera:id:info:";
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    @Resource
    private IdGeneratorConfig config;
    
    /**
     * Lua脚本：批量获取ID
     */
    private static final String BATCH_INCR_SCRIPT = 
        "local key = KEYS[1]\n" +
        "local infoKey = KEYS[2]\n" +
        "local count = tonumber(ARGV[1])\n" +
        "local expireTime = tonumber(ARGV[2])\n" +
        "local currentTime = tonumber(ARGV[3])\n" +
        "\n" +
        "-- 获取当前值并增加count\n" +
        "local currentValue = redis.call('INCRBY', key, count)\n" +
        "redis.call('EXPIRE', key, expireTime)\n" +
        "\n" +
        "-- 更新信息\n" +
        "redis.call('HSET', infoKey, 'maxId', currentValue, 'totalGenerated', redis.call('HGET', infoKey, 'totalGenerated') + count, 'updateTime', currentTime)\n" +
        "redis.call('EXPIRE', infoKey, expireTime)\n" +
        "\n" +
        "-- 返回起始ID和结束ID\n" +
        "return {currentValue - count + 1, currentValue}";
    
    @Override
    public Long generateId(String businessType) {
        String key = ID_KEY_PREFIX + businessType;
        String infoKey = ID_INFO_KEY_PREFIX + businessType;
        
        try {
            Long id = redisTemplate.opsForValue().increment(key, 1);
            if (id == null) {
                throw new RuntimeException("Failed to generate ID for business type: " + businessType);
            }
            
            // 设置过期时间
            redisTemplate.expire(key, config.getRedis().getExpireTime(), TimeUnit.SECONDS);
            
            // 更新信息
            updateIdInfo(infoKey, id, 1L);
            
            log.debug("Generated ID {} for business type: {}", id, businessType);
            return id;
        } catch (Exception e) {
            log.error("Failed to generate ID for business type: {}", businessType, e);
            throw new RuntimeException("Failed to generate ID", e);
        }
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
    public IdSegment generateIdSegment(String businessType, int segmentSize) {
        if (segmentSize <= 0) {
            throw new IllegalArgumentException("Segment size must be positive");
        }
        
        String key = ID_KEY_PREFIX + businessType;
        String infoKey = ID_INFO_KEY_PREFIX + businessType;
        
        try {
            DefaultRedisScript<List> script = new DefaultRedisScript<>();
            script.setScriptText(BATCH_INCR_SCRIPT);
            script.setResultType(List.class);
            
            List<String> keys = List.of(key, infoKey);
            Object[] args = {segmentSize, config.getRedis().getExpireTime(), System.currentTimeMillis()};
            
            @SuppressWarnings("unchecked")
            List<Long> result = (List<Long>) redisTemplate.execute(script, keys, args);
            
            if (result == null || result.size() != 2) {
                throw new RuntimeException("Invalid result from Redis script");
            }
            
            Long startId = result.get(0);
            Long endId = result.get(1);
            
            IdSegment segment = new IdSegment();
            segment.setStartId(startId);
            segment.setEndId(endId);
            segment.setBusinessType(businessType);
            segment.setSegmentSize(segmentSize);
            segment.setTimestamp(System.currentTimeMillis());
            
            log.debug("Generated ID segment [{}, {}] for business type: {}", startId, endId, businessType);
            return segment;
        } catch (Exception e) {
            log.error("Failed to generate ID segment for business type: {}", businessType, e);
            throw new RuntimeException("Failed to generate ID segment", e);
        }
    }
    
    @Override
    public void resetIdCounter(String businessType, Long startValue) {
        String key = ID_KEY_PREFIX + businessType;
        String infoKey = ID_INFO_KEY_PREFIX + businessType;
        
        try {
            redisTemplate.opsForValue().set(key, startValue - 1);
            redisTemplate.expire(key, config.getRedis().getExpireTime(), TimeUnit.SECONDS);
            
            // 重置信息
            redisTemplate.opsForHash().put(infoKey, "maxId", startValue - 1);
            redisTemplate.opsForHash().put(infoKey, "totalGenerated", 0L);
            redisTemplate.opsForHash().put(infoKey, "createTime", System.currentTimeMillis());
            redisTemplate.opsForHash().put(infoKey, "updateTime", System.currentTimeMillis());
            redisTemplate.expire(infoKey, config.getRedis().getExpireTime(), TimeUnit.SECONDS);
            
            log.info("Reset ID counter for business type: {} to start value: {}", businessType, startValue);
        } catch (Exception e) {
            log.error("Failed to reset ID counter for business type: {}", businessType, e);
            throw new RuntimeException("Failed to reset ID counter", e);
        }
    }
    
    @Override
    public IdStatus getIdStatus(String businessType) {
        String key = ID_KEY_PREFIX + businessType;
        String infoKey = ID_INFO_KEY_PREFIX + businessType;
        
        try {
            Object maxIdObj = redisTemplate.opsForValue().get(key);
            Long maxId = maxIdObj != null ? Long.valueOf(maxIdObj.toString()) : null;
            
            IdStatus status = new IdStatus();
            status.setBusinessType(businessType);
            status.setCurrentMaxId(maxId);
            status.setGeneratorType(IdGeneratorType.REDIS);
            status.setBatchSize(config.getBusinessTypeConfig(businessType).getBatchSize());
            status.setAvailable(maxId != null);
            
            // 从Redis获取详细信息
            if (redisTemplate.hasKey(infoKey)) {
                Object totalGenerated = redisTemplate.opsForHash().get(infoKey, "totalGenerated");
                Object createTime = redisTemplate.opsForHash().get(infoKey, "createTime");
                Object updateTime = redisTemplate.opsForHash().get(infoKey, "updateTime");
                
                if (totalGenerated != null) {
                    status.setTotalGenerated(Long.valueOf(totalGenerated.toString()));
                }
                if (createTime != null) {
                    status.setCreateTime(Long.valueOf(createTime.toString()));
                }
                if (updateTime != null) {
                    status.setUpdateTime(Long.valueOf(updateTime.toString()));
                }
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
            Set<String> keys = redisTemplate.keys(ID_KEY_PREFIX + "*");
            if (keys == null || keys.isEmpty()) {
                return Collections.emptyList();
            }
            
            List<IdStatus> statusList = new ArrayList<>();
            for (String key : keys) {
                String businessType = key.substring(ID_KEY_PREFIX.length());
                IdStatus status = getIdStatus(businessType);
                statusList.add(status);
            }
            
            return statusList;
        } catch (Exception e) {
            log.error("Failed to get all ID status", e);
            throw new RuntimeException("Failed to get all ID status", e);
        }
    }
    
    @Override
    public void initBusinessType(String businessType, Long startValue) {
        String key = ID_KEY_PREFIX + businessType;
        String infoKey = ID_INFO_KEY_PREFIX + businessType;
        
        try {
            if (!redisTemplate.hasKey(key)) {
                redisTemplate.opsForValue().set(key, startValue - 1);
                redisTemplate.expire(key, config.getRedis().getExpireTime(), TimeUnit.SECONDS);
                
                // 初始化信息
                redisTemplate.opsForHash().put(infoKey, "maxId", startValue - 1);
                redisTemplate.opsForHash().put(infoKey, "totalGenerated", 0L);
                redisTemplate.opsForHash().put(infoKey, "createTime", System.currentTimeMillis());
                redisTemplate.opsForHash().put(infoKey, "updateTime", System.currentTimeMillis());
                redisTemplate.expire(infoKey, config.getRedis().getExpireTime(), TimeUnit.SECONDS);
                
                log.info("Initialized business type: {} with start value: {}", businessType, startValue);
            }
        } catch (Exception e) {
            log.error("Failed to initialize business type: {}", businessType, e);
            throw new RuntimeException("Failed to initialize business type", e);
        }
    }
    
    @Override
    public void removeBusinessType(String businessType) {
        String key = ID_KEY_PREFIX + businessType;
        String infoKey = ID_INFO_KEY_PREFIX + businessType;
        
        try {
            redisTemplate.delete(key);
            redisTemplate.delete(infoKey);
            log.info("Removed business type: {}", businessType);
        } catch (Exception e) {
            log.error("Failed to remove business type: {}", businessType, e);
            throw new RuntimeException("Failed to remove business type", e);
        }
    }
    
    @Override
    public boolean supports(String businessType) {
        IdGeneratorConfig.BusinessTypeConfig config = this.config.getBusinessTypeConfig(businessType);
        return config.getStrategy() == IdGeneratorType.REDIS;
    }
    
    /**
     * 更新ID信息
     */
    private void updateIdInfo(String infoKey, Long maxId, Long increment) {
        try {
            redisTemplate.opsForHash().put(infoKey, "maxId", maxId);
            redisTemplate.opsForHash().increment(infoKey, "totalGenerated", increment);
            redisTemplate.opsForHash().put(infoKey, "updateTime", System.currentTimeMillis());
            redisTemplate.expire(infoKey, config.getRedis().getExpireTime(), TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Failed to update ID info for key: {}", infoKey, e);
        }
    }
} 