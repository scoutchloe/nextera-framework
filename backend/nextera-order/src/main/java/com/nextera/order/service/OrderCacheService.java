package com.nextera.order.service;

import com.nextera.order.dto.OrderStatisticsResponse;
import com.nextera.order.entity.Order;

import java.time.LocalDate;
import java.util.Map;

/**
 * 订单Redis缓存服务接口
 * 
 * @author Nextera Framework
 * @since 1.0.0
 */
public interface OrderCacheService {

    /**
     * 更新订单创建缓存
     * 
     * @param order 订单实体
     */
    void updateOrderCreatedCache(Order order);

    /**
     * 更新订单状态变更缓存
     * 
     * @param orderId 订单ID
     * @param oldStatus 原状态
     * @param newStatus 新状态
     * @param orderAmount 订单金额
     */
    void updateOrderStatusCache(Long orderId, Integer oldStatus, Integer newStatus, Double orderAmount);

    /**
     * 获取今日实时统计
     * 
     * @return 今日统计数据
     */
    Map<String, Object> getTodayRealtimeStatistics();

    /**
     * 获取指定日期的统计数据
     * 
     * @param date 日期
     * @return 统计数据
     */
    OrderStatisticsResponse getDateStatistics(LocalDate date);

    /**
     * 获取用户今日统计
     * 
     * @param userId 用户ID
     * @return 用户统计数据
     */
    Map<String, Object> getUserTodayStatistics(Long userId);

    /**
     * 获取商品销售实时排行
     * 
     * @param limit 返回条数
     * @return 商品排行数据
     */
    Map<String, Object> getProductRealtimeRanking(Integer limit);

    /**
     * 更新商品销售统计
     * 
     * @param productId 商品ID
     * @param productName 商品名称
     * @param quantity 购买数量
     * @param amount 销售金额
     */
    void updateProductSalesCache(Long productId, String productName, Integer quantity, Double amount);

    /**
     * 获取实时监控数据
     * 
     * @return 监控数据
     */
    Map<String, Object> getRealtimeMonitoringData();

    /**
     * 预热缓存
     * 
     * @param days 预热天数
     * @return 预热结果
     */
    Map<String, Object> warmupCache(Integer days);

    /**
     * 清理过期缓存
     * 
     * @param beforeDays 清理几天前的数据
     * @return 清理结果
     */
    Map<String, Object> cleanExpiredCache(Integer beforeDays);

    /**
     * 获取缓存统计信息
     * 
     * @return 缓存统计
     */
    Map<String, Object> getCacheStatistics();

    /**
     * 刷新指定日期的缓存
     * 
     * @param date 日期
     * @return 刷新结果
     */
    Map<String, Object> refreshDateCache(LocalDate date);

    /**
     * 增量更新用户统计
     * 
     * @param userId 用户ID
     * @param orderCount 订单数量增量
     * @param orderAmount 订单金额增量
     */
    void incrementUserStatistics(Long userId, Integer orderCount, Double orderAmount);

    /**
     * 获取热点数据
     * 
     * @return 热点数据
     */
    Map<String, Object> getHotspotData();

    /**
     * 设置缓存报警阈值
     * 
     * @param metric 指标名称
     * @param threshold 阈值
     */
    void setCacheAlertThreshold(String metric, Double threshold);
} 