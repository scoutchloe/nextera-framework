package com.nextera.order.service.impl;

import com.nextera.order.dto.OrderAnalysisResponse;
import com.nextera.order.dto.OrderStatisticsResponse;
import com.nextera.order.service.OrderAnalysisService;
import com.nextera.order.service.OrderCacheService;
import com.nextera.order.service.OrderElasticsearchService;
import com.nextera.order.service.UnifiedOrderQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 统一订单查询服务实现类（智能路由）
 * 
 * @author Nextera Framework
 * @since 1.0.0
 */
@Slf4j
@Service
public class UnifiedOrderQueryServiceImpl implements UnifiedOrderQueryService {

    @Autowired
    private OrderAnalysisService orderAnalysisService;

    @Autowired(required = false)
    private OrderElasticsearchService orderElasticsearchService;

    @Autowired
    private OrderCacheService orderCacheService;

    @Override
    public OrderStatisticsResponse getOrderStatistics(LocalDate startDate, LocalDate endDate, QueryStrategy strategy) {
        QueryStrategy finalStrategy = strategy == QueryStrategy.AUTO ? selectOptimalStrategy(startDate, endDate, "statistics") : strategy;
        
        long startTime = System.currentTimeMillis();
        try {
            OrderStatisticsResponse result = switch (finalStrategy) {
                case CACHE_FIRST -> getStatisticsFromCache(startDate, endDate);
                case ELASTICSEARCH -> orderElasticsearchService != null ? 
                    orderElasticsearchService.getOrderStatisticsByES(startDate, endDate) : 
                    orderAnalysisService.getOrderStatistics(startDate, endDate);
                case DATABASE -> orderAnalysisService.getOrderStatistics(startDate, endDate);
                default -> orderAnalysisService.getOrderStatistics(startDate, endDate);
            };
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("订单统计查询完成，策略：{}，耗时：{}ms", finalStrategy, duration);
            
            return result;
        } catch (Exception e) {
            log.error("订单统计查询失败，策略：{}", finalStrategy, e);
            // 降级到数据库查询
            return orderAnalysisService.getOrderStatistics(startDate, endDate);
        }
    }

    @Override
    public OrderStatisticsResponse getUserOrderStatistics(Long userId, LocalDate startDate, LocalDate endDate, QueryStrategy strategy) {
        QueryStrategy finalStrategy = strategy == QueryStrategy.AUTO ? selectOptimalStrategy(startDate, endDate, "user_statistics") : strategy;
        
        long startTime = System.currentTimeMillis();
        try {
            OrderStatisticsResponse result = switch (finalStrategy) {
                case CACHE_FIRST -> getUserStatisticsFromCache(userId, startDate, endDate);
                case ELASTICSEARCH -> orderElasticsearchService != null ? 
                    orderElasticsearchService.getUserOrderStatisticsByES(userId, startDate, endDate) : 
                    orderAnalysisService.getUserOrderStatistics(userId, startDate, endDate);
                case DATABASE -> orderAnalysisService.getUserOrderStatistics(userId, startDate, endDate);
                default -> orderAnalysisService.getUserOrderStatistics(userId, startDate, endDate);
            };
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("用户订单统计查询完成，用户ID：{}，策略：{}，耗时：{}ms", userId, finalStrategy, duration);
            
            return result;
        } catch (Exception e) {
            log.error("用户订单统计查询失败，用户ID：{}，策略：{}", userId, finalStrategy, e);
            // 降级到数据库查询
            return orderAnalysisService.getUserOrderStatistics(userId, startDate, endDate);
        }
    }

    @Override
    public List<OrderAnalysisResponse> getOrderTrend(LocalDate startDate, LocalDate endDate, QueryStrategy strategy) {
        QueryStrategy finalStrategy = strategy == QueryStrategy.AUTO ? selectOptimalStrategy(startDate, endDate, "trend") : strategy;
        
        long startTime = System.currentTimeMillis();
        try {
            List<OrderAnalysisResponse> result = switch (finalStrategy) {
                case ELASTICSEARCH -> orderElasticsearchService != null ? 
                    orderElasticsearchService.getOrderTrendByES(startDate, endDate) : 
                    orderAnalysisService.getOrderTrend(startDate, endDate);
                case DATABASE -> orderAnalysisService.getOrderTrend(startDate, endDate);
                default -> orderElasticsearchService != null ? 
                    orderElasticsearchService.getOrderTrendByES(startDate, endDate) : 
                    orderAnalysisService.getOrderTrend(startDate, endDate);
            };
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("订单趋势分析完成，策略：{}，耗时：{}ms", finalStrategy, duration);
            
            return result;
        } catch (Exception e) {
            log.error("订单趋势分析失败，策略：{}", finalStrategy, e);
            // 降级到数据库查询
            return orderAnalysisService.getOrderTrend(startDate, endDate);
        }
    }

    @Override
    public List<Map<String, Object>> getProductSalesRanking(LocalDate startDate, LocalDate endDate, Integer limit, QueryStrategy strategy) {
        QueryStrategy finalStrategy = strategy == QueryStrategy.AUTO ? selectOptimalStrategy(startDate, endDate, "ranking") : strategy;
        
        long startTime = System.currentTimeMillis();
        try {
            List<Map<String, Object>> result = switch (finalStrategy) {
                case CACHE_FIRST -> getProductRankingFromCache(startDate, endDate, limit);
                case ELASTICSEARCH -> orderElasticsearchService != null ? 
                    orderElasticsearchService.getProductSalesRankingByES(startDate, endDate, limit) : 
                    orderAnalysisService.getProductSalesRanking(startDate, endDate, limit);
                case DATABASE -> orderAnalysisService.getProductSalesRanking(startDate, endDate, limit);
                default -> orderElasticsearchService != null ? 
                    orderElasticsearchService.getProductSalesRankingByES(startDate, endDate, limit) : 
                    orderAnalysisService.getProductSalesRanking(startDate, endDate, limit);
            };
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("商品销售排行查询完成，策略：{}，耗时：{}ms", finalStrategy, duration);
            
            return result;
        } catch (Exception e) {
            log.error("商品销售排行查询失败，策略：{}", finalStrategy, e);
            // 降级到数据库查询
            return orderAnalysisService.getProductSalesRanking(startDate, endDate, limit);
        }
    }

    @Override
    public Map<String, Object> getRealtimeMonitoringData() {
        return orderCacheService.getRealtimeMonitoringData();
    }

    @Override
    public Map<String, Object> getTodayRealtimeStatistics() {
        return orderCacheService.getTodayRealtimeStatistics();
    }

    @Override
    public Map<String, Object> complexQuery(Map<String, Object> queryParams) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 根据查询复杂度选择策略
            QueryStrategy strategy = analyzeQueryComplexity(queryParams);
            
            long startTime = System.currentTimeMillis();
            
            // 执行复杂查询
            switch (strategy) {
                case ELASTICSEARCH -> {
                    // 使用ElasticSearch进行复杂查询
                    if (orderElasticsearchService != null) {
                        result.put("data", orderElasticsearchService.advancedSearch(queryParams));
                        result.put("strategy", "ELASTICSEARCH");
                    } else {
                        result.put("data", "ElasticSearch服务不可用，降级为默认查询");
                        result.put("strategy", "FALLBACK");
                    }
                }
                case DATABASE -> {
                    // 使用数据库进行查询
                    result.put("data", "数据库复杂查询结果");
                    result.put("strategy", "DATABASE");
                }
                default -> {
                    result.put("data", "默认查询结果");
                    result.put("strategy", "DEFAULT");
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            result.put("queryTime", duration);
            result.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            log.error("复杂查询执行失败", e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> performanceBenchmark(String testType, Integer concurrency, Integer duration) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            LocalDate today = LocalDate.now();
            LocalDate yesterday = today.minusDays(1);
            
            // 模拟性能测试
            long startTime = System.currentTimeMillis();
            
            // 简化的性能测试
            for (int i = 0; i < concurrency; i++) {
                switch (testType) {
                    case "cache" -> orderCacheService.getTodayRealtimeStatistics();
                    case "elasticsearch" ->  orderElasticsearchService.getOrderStatisticsByES(yesterday, today);
                    case "database" -> orderAnalysisService.getOrderStatistics(yesterday, today);
                }
            }
            
            long totalTime = System.currentTimeMillis() - startTime;
            
            result.put("testType", testType);
            result.put("concurrency", concurrency);
            result.put("duration", duration);
            result.put("totalTime", totalTime);
            result.put("avgResponseTime", totalTime / concurrency);
            result.put("qps", (concurrency * 1000.0) / totalTime);
            
        } catch (Exception e) {
            log.error("性能基准测试失败", e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> queryPerformanceAnalysis(String queryType, Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        // 测试不同策略的性能
        Map<String, Long> performanceResults = new HashMap<>();
        
        try {
            // 测试缓存查询
            long cacheStart = System.currentTimeMillis();
            orderCacheService.getTodayRealtimeStatistics();
            performanceResults.put("cache", System.currentTimeMillis() - cacheStart);
            
            // 测试ElasticSearch查询
            if (orderElasticsearchService != null) {
                long esStart = System.currentTimeMillis();
                orderElasticsearchService.getOrderStatisticsByES(yesterday, today);
                performanceResults.put("elasticsearch", System.currentTimeMillis() - esStart);
            } else {
                performanceResults.put("elasticsearch", -1L); // 表示服务不可用
            }
            
            // 测试数据库查询
            long dbStart = System.currentTimeMillis();
            orderAnalysisService.getOrderStatistics(yesterday, today);
            performanceResults.put("database", System.currentTimeMillis() - dbStart);
            
            result.put("performanceResults", performanceResults);
            result.put("recommendedStrategy", getRecommendedStrategy(performanceResults));
            
        } catch (Exception e) {
            log.error("查询性能分析失败", e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // 检查Redis健康状态
            long redisStart = System.currentTimeMillis();
            orderCacheService.getCacheStatistics();
            long redisTime = System.currentTimeMillis() - redisStart;
            
            health.put("redis", Map.of(
                    "status", redisTime < 100 ? "healthy" : "slow",
                    "responseTime", redisTime
            ));
            
            // 检查ElasticSearch健康状态
            if (orderElasticsearchService != null) {
                long esStart = System.currentTimeMillis();
                orderElasticsearchService.findOrdersByUserId(1L);
                long esTime = System.currentTimeMillis() - esStart;
                
                health.put("elasticsearch", Map.of(
                        "status", esTime < 1000 ? "healthy" : "slow",
                        "responseTime", esTime
                ));
            } else {
                health.put("elasticsearch", Map.of(
                        "status", "disabled",
                        "message", "ElasticSearch service is not available"
                ));
            }
            
            // 检查数据库健康状态
            long dbStart = System.currentTimeMillis();
            orderAnalysisService.getRealtimeOrderMonitoring();
            long dbTime = System.currentTimeMillis() - dbStart;
            
            health.put("database", Map.of(
                    "status", dbTime < 500 ? "healthy" : "slow",
                    "responseTime", dbTime
            ));
            
            health.put("overall", "healthy");
            health.put("timestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            log.error("健康检查失败", e);
            health.put("overall", "unhealthy");
            health.put("error", e.getMessage());
        }
        
        return health;
    }

    @Override
    public Map<String, Object> getQueryStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalQueries", 1000); // 模拟数据
        stats.put("cacheHitRate", "92%");
        stats.put("avgResponseTime", "150ms");
        stats.put("errorRate", "0.5%");
        stats.put("timestamp", System.currentTimeMillis());
        
        return stats;
    }

    /**
     * 选择最优查询策略
     */
    private QueryStrategy selectOptimalStrategy(LocalDate startDate, LocalDate endDate, String queryType) {
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        LocalDate today = LocalDate.now();
        long daysFromToday = ChronoUnit.DAYS.between(startDate, today);
        
        // 今日数据优先使用缓存
        if (startDate.equals(today) && endDate.equals(today)) {
            return QueryStrategy.CACHE_FIRST;
        }
        
        // 近期数据且查询范围不大，使用ElasticSearch
        if (daysFromToday <= 30 && daysBetween <= 30) {
            return QueryStrategy.ELASTICSEARCH;
        }
        
        // 历史数据或大范围查询，使用数据库
        return QueryStrategy.DATABASE;
    }

    /**
     * 从缓存获取统计数据
     */
    private OrderStatisticsResponse getStatisticsFromCache(LocalDate startDate, LocalDate endDate) {
        if (startDate.equals(endDate) && startDate.equals(LocalDate.now())) {
            return orderCacheService.getDateStatistics(startDate);
        }
        // 跨日期查询，降级到ElasticSearch
        return orderElasticsearchService != null ? 
            orderElasticsearchService.getOrderStatisticsByES(startDate, endDate) : 
            orderAnalysisService.getOrderStatistics(startDate, endDate);
    }

    /**
     * 从缓存获取用户统计数据
     */
    private OrderStatisticsResponse getUserStatisticsFromCache(Long userId, LocalDate startDate, LocalDate endDate) {
        if (startDate.equals(endDate) && startDate.equals(LocalDate.now())) {
            // 从缓存获取今日用户统计，这里简化处理
            return orderCacheService.getDateStatistics(startDate);
        }
        // 跨日期查询，降级到ElasticSearch
        return orderElasticsearchService != null ? 
            orderElasticsearchService.getUserOrderStatisticsByES(userId, startDate, endDate) : 
            orderAnalysisService.getUserOrderStatistics(userId, startDate, endDate);
    }

    /**
     * 从缓存获取商品排行
     */
    private List<Map<String, Object>> getProductRankingFromCache(LocalDate startDate, LocalDate endDate, Integer limit) {
        if (startDate.equals(endDate) && startDate.equals(LocalDate.now())) {
            // 从缓存获取今日商品排行，这里简化处理
            Map<String, Object> ranking = orderCacheService.getProductRealtimeRanking(limit);
            return List.of(ranking);
        }
        // 跨日期查询，降级到ElasticSearch
        return orderElasticsearchService != null ? 
            orderElasticsearchService.getProductSalesRankingByES(startDate, endDate, limit) : 
            orderAnalysisService.getProductSalesRanking(startDate, endDate, limit);
    }

    /**
     * 分析查询复杂度
     */
    private QueryStrategy analyzeQueryComplexity(Map<String, Object> queryParams) {
        int complexity = 0;
        
        if (queryParams.containsKey("filters")) complexity += 2;
        if (queryParams.containsKey("aggregations")) complexity += 3;
        if (queryParams.containsKey("sorting")) complexity += 1;
        if (queryParams.containsKey("fullTextSearch")) complexity += 4;
        
        return complexity > 5 ? QueryStrategy.ELASTICSEARCH : QueryStrategy.DATABASE;
    }

    /**
     * 获取推荐策略
     */
    private String getRecommendedStrategy(Map<String, Long> performanceResults) {
        return performanceResults.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("database");
    }
} 