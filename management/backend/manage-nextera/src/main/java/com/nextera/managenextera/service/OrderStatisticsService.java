package com.nextera.managenextera.service;

import com.nextera.managenextera.dto.OrderStatisticsDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 订单统计服务接口
 *
 * @author nextera
 * @since 2025-01-01
 */
public interface OrderStatisticsService {

    /**
     * 获取概览统计（Dashboard用）
     *
     * @return 概览统计
     */
    OrderStatisticsDTO.OverviewStatistics getOverviewStatistics();

    /**
     * 获取实时统计
     *
     * @return 实时统计
     */
    OrderStatisticsDTO.RealtimeStatistics getRealtimeStatistics();

    /**
     * 获取订单趋势（按日期）
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 趋势数据
     */
    List<OrderStatisticsDTO.TrendStatistics> getOrderTrend(LocalDate startDate, LocalDate endDate);

    /**
     * 获取订单状态分布
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 状态分布
     */
    List<OrderStatisticsDTO.StatusDistribution> getOrderStatusDistribution(LocalDate startDate, LocalDate endDate);

    /**
     * 获取商品销售排行榜
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param limit 返回条数
     * @return 销售排行
     */
    List<OrderStatisticsDTO.ProductSalesRanking> getProductSalesRanking(LocalDate startDate, LocalDate endDate, Integer limit);

    /**
     * 获取用户订单分析
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param limit 返回条数
     * @return 用户分析
     */
    List<OrderStatisticsDTO.UserOrderAnalysis> getUserOrderAnalysis(LocalDate startDate, LocalDate endDate, Integer limit);

    /**
     * 获取订单金额分布
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 金额分布
     */
    Map<String, Long> getOrderAmountDistribution(LocalDate startDate, LocalDate endDate);

    /**
     * 刷新统计缓存
     *
     * @return 是否成功
     */
    boolean refreshStatisticsCache();

    /**
     * 获取缓存的统计数据
     *
     * @param cacheKey 缓存键
     * @return 统计数据
     */
    Object getCachedStatistics(String cacheKey);

    /**
     * 设置统计缓存
     *
     * @param cacheKey 缓存键
     * @param data 数据
     * @param expireSeconds 过期秒数
     */
    void setCachedStatistics(String cacheKey, Object data, long expireSeconds);
} 