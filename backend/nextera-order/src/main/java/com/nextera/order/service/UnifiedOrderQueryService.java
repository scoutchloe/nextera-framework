package com.nextera.order.service;

import com.nextera.order.dto.OrderAnalysisResponse;
import com.nextera.order.dto.OrderStatisticsResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 统一订单查询服务接口（路由选择器）
 * 
 * @author Nextera Framework
 * @since 1.0.0
 */
public interface UnifiedOrderQueryService {

    /**
     * 查询策略枚举
     */
    enum QueryStrategy {
        /** 实时缓存优先 */
        CACHE_FIRST,
        /** ElasticSearch聚合 */
        ELASTICSEARCH,
        /** 数据库直查 */
        DATABASE,
        /** 自动选择 */
        AUTO
    }

    /**
     * 获取订单统计数据（智能路由）
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param strategy 查询策略
     * @return 统计结果
     */
    OrderStatisticsResponse getOrderStatistics(LocalDate startDate, LocalDate endDate, QueryStrategy strategy);

    /**
     * 获取用户订单统计（智能路由）
     * 
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param strategy 查询策略
     * @return 用户统计结果
     */
    OrderStatisticsResponse getUserOrderStatistics(Long userId, LocalDate startDate, LocalDate endDate, QueryStrategy strategy);

    /**
     * 获取订单趋势分析（智能路由）
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param strategy 查询策略
     * @return 趋势分析结果
     */
    List<OrderAnalysisResponse> getOrderTrend(LocalDate startDate, LocalDate endDate, QueryStrategy strategy);

    /**
     * 获取商品销售排行榜（智能路由）
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param limit 返回条数
     * @param strategy 查询策略
     * @return 商品销售排行
     */
    List<Map<String, Object>> getProductSalesRanking(LocalDate startDate, LocalDate endDate, Integer limit, QueryStrategy strategy);

    /**
     * 获取实时监控数据
     * 
     * @return 实时监控数据
     */
    Map<String, Object> getRealtimeMonitoringData();

    /**
     * 获取今日实时统计
     * 
     * @return 今日统计数据
     */
    Map<String, Object> getTodayRealtimeStatistics();

    /**
     * 复杂查询（自动选择最优策略）
     * 
     * @param queryParams 查询参数
     * @return 查询结果
     */
    Map<String, Object> complexQuery(Map<String, Object> queryParams);

    /**
     * 性能基准测试
     * 
     * @param testType 测试类型
     * @param concurrency 并发数
     * @param duration 持续时间（秒）
     * @return 测试结果
     */
    Map<String, Object> performanceBenchmark(String testType, Integer concurrency, Integer duration);

    /**
     * 查询性能分析
     * 
     * @param queryType 查询类型
     * @param params 查询参数
     * @return 性能分析结果
     */
    Map<String, Object> queryPerformanceAnalysis(String queryType, Map<String, Object> params);

    /**
     * 系统健康检查
     * 
     * @return 健康状态
     */
    Map<String, Object> healthCheck();

    /**
     * 获取查询统计信息
     * 
     * @return 查询统计
     */
    Map<String, Object> getQueryStatistics();
} 