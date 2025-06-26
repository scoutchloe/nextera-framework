package com.nextera.managenextera.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存服务接口
 *
 * @author nextera
 * @since 2025-01-01
 */
public interface RedisService {

    /**
     * 设置缓存
     */
    void set(String key, Object value);

    /**
     * 设置缓存并指定过期时间
     */
    void set(String key, Object value, long timeout, TimeUnit unit);

    /**
     * 获取缓存
     */
    Object get(String key);

    /**
     * 获取指定类型的缓存
     */
    <T> T get(String key, Class<T> clazz);

    /**
     * 删除缓存
     */
    void delete(String key);

    /**
     * 批量删除缓存
     */
    void delete(List<String> keys);

    /**
     * 判断缓存是否存在
     */
    boolean hasKey(String key);

    /**
     * 设置过期时间
     */
    boolean expire(String key, long timeout, TimeUnit unit);

    /**
     * 获取过期时间
     */
    long getExpire(String key);

    /**
     * 递增
     */
    long increment(String key);

    /**
     * 递增指定步长
     */
    long increment(String key, long delta);

    /**
     * 递减
     */
    long decrement(String key);

    /**
     * 递减指定步长
     */
    long decrement(String key, long delta);
} 