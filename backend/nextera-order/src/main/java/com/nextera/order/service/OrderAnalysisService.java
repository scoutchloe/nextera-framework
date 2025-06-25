package com.nextera.order.service;

import com.nextera.order.dto.OrderAnalysisRequest;
import com.nextera.order.dto.OrderAnalysisResponse;
import com.nextera.order.dto.OrderStatisticsResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 订单数据分析服务接口
 * 
 * @author Nextera Framework
 * @since 1.0.0
 */
public interface OrderAnalysisService {

    /**
     * 获取订单统计数据（跨分片聚合）
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计结果
     */
    OrderStatisticsResponse getOrderStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 获取用户订单统计（单分片查询）
     * 
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 用户统计结果
     */
    OrderStatisticsResponse getUserOrderStatistics(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取订单趋势分析（按日统计）
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 每日统计数据
     */
    List<OrderAnalysisResponse> getOrderTrend(LocalDate startDate, LocalDate endDate);

    /**
     * 获取商品销售排行榜（跨分片聚合）
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param limit 返回条数
     * @return 商品销售排行
     */
    List<Map<String, Object>> getProductSalesRanking(LocalDate startDate, LocalDate endDate, Integer limit);

    /**
     * 获取订单金额分布统计
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 金额分布统计
     */
    Map<String, Long> getOrderAmountDistribution(LocalDate startDate, LocalDate endDate);

    /**
     * 实时订单监控数据
     * 
     * @return 实时监控数据
     */
    Map<String, Object> getRealtimeOrderMonitoring();
} 