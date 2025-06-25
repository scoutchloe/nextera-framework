package com.nextera.order.controller;

import com.nextera.common.core.Result;
import com.nextera.order.dto.OrderAnalysisResponse;
import com.nextera.order.dto.OrderStatisticsResponse;
import com.nextera.order.service.OrderCacheService;
import com.nextera.order.service.OrderElasticsearchService;
import com.nextera.order.service.UnifiedOrderQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 订单分析API控制器
 * 
 * @author Nextera Framework
 * @since 1.0.0
 */
@Tag(name = "订单分析", description = "订单数据分析和统计API")
@Slf4j
@RestController
@RequestMapping("/api/order/analysis")
public class OrderAnalysisController {

    @Autowired
    private UnifiedOrderQueryService unifiedOrderQueryService;

    @Autowired
    private OrderElasticsearchService orderElasticsearchService;

    @Autowired
    private OrderCacheService orderCacheService;

    /**
     * 获取订单统计数据（智能路由）
     */
    @Operation(summary = "获取订单统计数据", description = "支持多种查询策略的订单统计")
    @GetMapping("/statistics")
    public Result<OrderStatisticsResponse> getOrderStatistics(
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "查询策略") @RequestParam(defaultValue = "AUTO") UnifiedOrderQueryService.QueryStrategy strategy) {
        
        OrderStatisticsResponse result = unifiedOrderQueryService.getOrderStatistics(startDate, endDate, strategy);
        return Result.success(result);
    }

    /**
     * 获取用户订单统计
     */
    @Operation(summary = "获取用户订单统计", description = "获取指定用户的订单统计数据")
    @GetMapping("/user-statistics")
    public Result<OrderStatisticsResponse> getUserOrderStatistics(
            @Parameter(description = "用户ID") @RequestParam Long userId,
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "查询策略") @RequestParam(defaultValue = "AUTO") UnifiedOrderQueryService.QueryStrategy strategy) {
        
        OrderStatisticsResponse result = unifiedOrderQueryService.getUserOrderStatistics(userId, startDate, endDate, strategy);
        return Result.success(result);
    }

    /**
     * 获取订单趋势分析
     */
    @Operation(summary = "获取订单趋势分析", description = "获取指定时间范围的订单趋势分析")
    @GetMapping("/trend")
    public Result<List<OrderAnalysisResponse>> getOrderTrend(
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "查询策略") @RequestParam(defaultValue = "ELASTICSEARCH") UnifiedOrderQueryService.QueryStrategy strategy) {
        
        List<OrderAnalysisResponse> result = unifiedOrderQueryService.getOrderTrend(startDate, endDate, strategy);
        return Result.success(result);
    }

    /**
     * 获取商品销售排行榜
     */
    @Operation(summary = "获取商品销售排行榜", description = "获取指定时间范围的商品销售排行榜")
    @GetMapping("/product-ranking")
    public Result<List<Map<String, Object>>> getProductSalesRanking(
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @Parameter(description = "返回条数") @RequestParam(defaultValue = "20") Integer limit,
            @Parameter(description = "查询策略") @RequestParam(defaultValue = "AUTO") UnifiedOrderQueryService.QueryStrategy strategy) {
        
        List<Map<String, Object>> result = unifiedOrderQueryService.getProductSalesRanking(startDate, endDate, limit, strategy);
        return Result.success(result);
    }

    /**
     * 获取今日实时统计
     */
    @Operation(summary = "获取今日实时统计", description = "获取今日实时订单统计数据")
    @GetMapping("/today-realtime")
    public Result<Map<String, Object>> getTodayRealtimeStatistics() {
        Map<String, Object> result = unifiedOrderQueryService.getTodayRealtimeStatistics();
        return Result.success(result);
    }

    /**
     * 获取实时监控数据
     */
    @Operation(summary = "获取实时监控数据", description = "获取系统实时监控数据")
    @GetMapping("/realtime-monitoring")
    public Result<Map<String, Object>> getRealtimeMonitoringData() {
        Map<String, Object> result = unifiedOrderQueryService.getRealtimeMonitoringData();
        return Result.success(result);
    }

    /**
     * ElasticSearch聚合查询
     */
    @Operation(summary = "ElasticSearch聚合查询", description = "使用ElasticSearch进行聚合查询")
    @GetMapping("/es/statistics")
    public Result<OrderStatisticsResponse> getESStatistics(
            @Parameter(description = "开始日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        OrderStatisticsResponse result = orderElasticsearchService.getOrderStatisticsByES(startDate, endDate);
        return Result.success(result);
    }

    /**
     * Redis缓存查询
     */
    @Operation(summary = "Redis缓存查询", description = "使用Redis缓存进行快速查询")
    @GetMapping("/cache/statistics")
    public Result<OrderStatisticsResponse> getCacheStatistics(
            @Parameter(description = "日期") @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        
        OrderStatisticsResponse result = orderCacheService.getDateStatistics(date);
        return Result.success(result);
    }

    /**
     * 复杂查询
     */
    @Operation(summary = "复杂查询", description = "支持复杂条件的订单查询")
    @PostMapping("/complex-query")
    public Result<Map<String, Object>> complexQuery(@RequestBody Map<String, Object> queryParams) {
        Map<String, Object> result = unifiedOrderQueryService.complexQuery(queryParams);
        return Result.success(result);
    }

    /**
     * 性能基准测试
     */
    @Operation(summary = "性能基准测试", description = "不同查询策略的性能测试")
    @GetMapping("/benchmark")
    public Result<Map<String, Object>> performanceBenchmark(
            @Parameter(description = "测试类型") @RequestParam String testType,
            @Parameter(description = "并发数") @RequestParam(defaultValue = "10") Integer concurrency,
            @Parameter(description = "持续时间（秒）") @RequestParam(defaultValue = "60") Integer duration) {
        
        Map<String, Object> result = unifiedOrderQueryService.performanceBenchmark(testType, concurrency, duration);
        return Result.success(result);
    }

    /**
     * 系统健康检查
     */
    @Operation(summary = "系统健康检查", description = "检查ElasticSearch和Redis的健康状态")
    @GetMapping("/health")
    public Result<Map<String, Object>> healthCheck() {
        Map<String, Object> result = unifiedOrderQueryService.healthCheck();
        return Result.success(result);
    }

    /**
     * 缓存管理
     */
    @Operation(summary = "缓存预热", description = "预热Redis缓存")
    @PostMapping("/cache/warmup")
    public Result<Map<String, Object>> warmupCache(
            @Parameter(description = "预热天数") @RequestParam(defaultValue = "7") Integer days) {
        
        Map<String, Object> result = orderCacheService.warmupCache(days);
        return Result.success(result);
    }

    /**
     * 清理过期缓存
     */
    @Operation(summary = "清理过期缓存", description = "清理过期的Redis缓存数据")
    @PostMapping("/cache/cleanup")
    public Result<Map<String, Object>> cleanupCache(
            @Parameter(description = "清理几天前的数据") @RequestParam(defaultValue = "30") Integer beforeDays) {
        
        Map<String, Object> result = orderCacheService.cleanExpiredCache(beforeDays);
        return Result.success(result);
    }

    /**
     * 获取查询统计信息
     */
    @Operation(summary = "获取查询统计信息", description = "获取系统查询统计和性能信息")
    @GetMapping("/query-statistics")
    public Result<Map<String, Object>> getQueryStatistics() {
        Map<String, Object> result = unifiedOrderQueryService.getQueryStatistics();
        return Result.success(result);
    }
} 