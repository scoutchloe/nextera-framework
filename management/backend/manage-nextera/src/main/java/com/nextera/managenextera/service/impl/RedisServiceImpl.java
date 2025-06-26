package com.nextera.managenextera.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextera.managenextera.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存服务实现类
 *
 * @author nextera
 * @since 2025-01-01
 */
@Slf4j
@Service
public class RedisServiceImpl implements RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ObjectMapper redisObjectMapper;

    @Override
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            log.debug("Redis设置缓存成功: key={}", key);
        } catch (Exception e) {
            log.error("Redis设置缓存失败: key={}", key, e);
        }
    }

    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            log.debug("Redis设置缓存成功: key={}, timeout={} {}", key, timeout, unit);
        } catch (Exception e) {
            log.error("Redis设置缓存失败: key={}, timeout={} {}", key, timeout, unit, e);
        }
    }

    @Override
    public Object get(String key) {
        try {
            Object cachedObj = redisTemplate.opsForValue().get(key);
            if (cachedObj != null) {
                log.debug("Redis获取缓存成功: key={}", key);
            }
            return cachedObj;
        } catch (Exception e) {
            log.error("Redis获取缓存失败: key={}", key, e);
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object cachedObj = redisTemplate.opsForValue().get(key);
            if (cachedObj == null) {
                return null;
            }

            // 处理类型转换
            if (clazz.isInstance(cachedObj)) {
                log.debug("Redis获取缓存成功，类型匹配: key={}, type={}", key, clazz.getSimpleName());
                return (T) cachedObj;
            } else if (cachedObj instanceof LinkedHashMap) {
                // 处理反序列化为LinkedHashMap的情况
                log.debug("Redis缓存反序列化为LinkedHashMap，尝试转换为目标类型: key={}, type={}", key, clazz.getSimpleName());
                try {
                    String json = redisObjectMapper.writeValueAsString(cachedObj);
                    T result = redisObjectMapper.readValue(json, clazz);
                    log.debug("LinkedHashMap转换为目标类型成功: key={}, type={}", key, clazz.getSimpleName());
                    return result;
                } catch (Exception convertException) {
                    log.warn("LinkedHashMap转换为目标类型失败，删除缓存: key={}, type={}", key, clazz.getSimpleName(), convertException);
                    delete(key);
                    return null;
                }
            } else {
                log.warn("Redis缓存类型不匹配，删除缓存: key={}, expected={}, actual={}", 
                    key, clazz.getSimpleName(), cachedObj.getClass().getSimpleName());
                delete(key);
                return null;
            }
        } catch (Exception e) {
            log.error("Redis获取缓存失败: key={}, type={}", key, clazz.getSimpleName(), e);
            return null;
        }
    }

    @Override
    public void delete(String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            if (Boolean.TRUE.equals(deleted)) {
                log.debug("Redis删除缓存成功: key={}", key);
            } else {
                log.debug("Redis删除缓存，key不存在: key={}", key);
            }
        } catch (Exception e) {
            log.error("Redis删除缓存失败: key={}", key, e);
        }
    }

    @Override
    public void delete(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        try {
            Long deletedCount = redisTemplate.delete(keys);
            log.debug("Redis批量删除缓存成功: keys={}, deletedCount={}", keys.size(), deletedCount);
        } catch (Exception e) {
            log.error("Redis批量删除缓存失败: keys={}", keys, e);
        }
    }

    @Override
    public boolean hasKey(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Redis检查key是否存在失败: key={}", key, e);
            return false;
        }
    }

    @Override
    public boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            Boolean result = redisTemplate.expire(key, timeout, unit);
            if (Boolean.TRUE.equals(result)) {
                log.debug("Redis设置过期时间成功: key={}, timeout={} {}", key, timeout, unit);
            }
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Redis设置过期时间失败: key={}, timeout={} {}", key, timeout, unit, e);
            return false;
        }
    }

    @Override
    public long getExpire(String key) {
        try {
            Long expire = redisTemplate.getExpire(key);
            return expire != null ? expire : -1;
        } catch (Exception e) {
            log.error("Redis获取过期时间失败: key={}", key, e);
            return -1;
        }
    }

    @Override
    public long increment(String key) {
        return increment(key, 1);
    }

    @Override
    public long increment(String key, long delta) {
        try {
            Long result = redisTemplate.opsForValue().increment(key, delta);
            log.debug("Redis递增成功: key={}, delta={}, result={}", key, delta, result);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Redis递增失败: key={}, delta={}", key, delta, e);
            return 0;
        }
    }

    @Override
    public long decrement(String key) {
        return decrement(key, 1);
    }

    @Override
    public long decrement(String key, long delta) {
        try {
            Long result = redisTemplate.opsForValue().decrement(key, delta);
            log.debug("Redis递减成功: key={}, delta={}, result={}", key, delta, result);
            return result != null ? result : 0;
        } catch (Exception e) {
            log.error("Redis递减失败: key={}, delta={}", key, delta, e);
            return 0;
        }
    }
} 