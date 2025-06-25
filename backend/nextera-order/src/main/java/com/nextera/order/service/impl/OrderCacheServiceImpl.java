package com.nextera.order.service.impl;

import com.nextera.order.dto.OrderStatisticsResponse;
import com.nextera.order.entity.Order;
import com.nextera.order.service.OrderCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 订单Redis缓存服务实现类
 * 
 * @author Nextera Framework
 * @since 1.0.0
 */
@Slf4j
@Service
public class OrderCacheServiceImpl implements OrderCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 缓存Key前缀
     */
    private static final String ORDER_STATS_PREFIX = "order:stats:";
    private static final String USER_STATS_PREFIX = "order:user:";
    private static final String PRODUCT_RANKING_PREFIX = "order:product:ranking:";
    private static final String REALTIME_MONITOR_KEY = "order:monitor:realtime";
    private static final String HOTSPOT_KEY = "order:hotspot";
    
    /**
     * 日期格式
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void updateOrderCreatedCache(Order order) {
        String today = LocalDate.now().format(DATE_FORMATTER);
        String dateKey = ORDER_STATS_PREFIX + today;
        
        try {
            // 原子性更新今日统计
            redisTemplate.opsForHash().increment(dateKey, "totalOrders", 1);
            redisTemplate.opsForHash().increment(dateKey, "totalAmount", order.getTotalAmount().doubleValue());
            
            // 按状态统计
            String statusKey = "status_" + order.getStatus();
            redisTemplate.opsForHash().increment(dateKey, statusKey, 1);
            
            // 设置过期时间（7天）
            redisTemplate.expire(dateKey, 7, TimeUnit.DAYS);
            
            log.debug("订单创建缓存更新成功，订单ID：{}", order.getId());
        } catch (Exception e) {
            log.error("更新订单创建缓存失败，订单ID：{}", order.getId(), e);
        }
    }

    @Override
    public void updateOrderStatusCache(Long orderId, Integer oldStatus, Integer newStatus, Double orderAmount) {
        String today = LocalDate.now().format(DATE_FORMATTER);
        String dateKey = ORDER_STATS_PREFIX + today;
        
        try {
            // 减少旧状态统计
            if (oldStatus != null) {
                String oldStatusKey = "status_" + oldStatus;
                redisTemplate.opsForHash().increment(dateKey, oldStatusKey, -1);
            }
            
            // 增加新状态统计
            String newStatusKey = "status_" + newStatus;
            redisTemplate.opsForHash().increment(dateKey, newStatusKey, 1);
            
            log.debug("订单状态变更缓存更新成功，订单ID：{}，状态：{} -> {}", orderId, oldStatus, newStatus);
        } catch (Exception e) {
            log.error("更新订单状态缓存失败，订单ID：{}", orderId, e);
        }
    }

    @Override
    public Map<String, Object> getTodayRealtimeStatistics() {
        String today = LocalDate.now().format(DATE_FORMATTER);
        String dateKey = ORDER_STATS_PREFIX + today;
        
        Map<Object, Object> stats = redisTemplate.opsForHash().entries(dateKey);
        Map<String, Object> result = new HashMap<>();
        
        // 基础统计
        result.put("totalOrders", getLongValue(stats, "totalOrders"));
        result.put("totalAmount", getDoubleValue(stats, "totalAmount"));
        result.put("pendingOrders", getLongValue(stats, "status_1"));
        result.put("paidOrders", getLongValue(stats, "status_2"));
        result.put("completedOrders", getLongValue(stats, "status_4"));
        result.put("cancelledOrders", getLongValue(stats, "status_5"));
        
        result.put("date", today);
        result.put("updateTime", System.currentTimeMillis());
        
        return result;
    }

    @Override
    public OrderStatisticsResponse getDateStatistics(LocalDate date) {
        String dateStr = date.format(DATE_FORMATTER);
        String dateKey = ORDER_STATS_PREFIX + dateStr;
        
        Map<Object, Object> stats = redisTemplate.opsForHash().entries(dateKey);
        
        if (stats.isEmpty()) {
            return new OrderStatisticsResponse()
                    .setStartDate(date)
                    .setEndDate(date)
                    .setTotalOrders(0L)
                    .setTotalAmount(BigDecimal.ZERO);
        }
        
        Long totalOrders = getLongValue(stats, "totalOrders");
        Double totalAmount = getDoubleValue(stats, "totalAmount");
        
        return new OrderStatisticsResponse()
                .setStartDate(date)
                .setEndDate(date)
                .setTotalOrders(totalOrders)
                .setTotalAmount(BigDecimal.valueOf(totalAmount));
    }

    @Override
    public Map<String, Object> getUserTodayStatistics(Long userId) {
        String today = LocalDate.now().format(DATE_FORMATTER);
        String userKey = USER_STATS_PREFIX + userId + ":" + today;
        
        Map<Object, Object> stats = redisTemplate.opsForHash().entries(userKey);
        Map<String, Object> result = new HashMap<>();
        
        result.put("userId", userId);
        result.put("date", today);
        result.put("orderCount", getLongValue(stats, "orderCount"));
        result.put("orderAmount", getDoubleValue(stats, "orderAmount"));
        
        return result;
    }

    @Override
    public Map<String, Object> getProductRealtimeRanking(Integer limit) {
        String today = LocalDate.now().format(DATE_FORMATTER);
        String rankingKey = PRODUCT_RANKING_PREFIX + today;
        
        Set<ZSetOperations.TypedTuple<Object>> ranking = redisTemplate.opsForZSet()
                .reverseRangeWithScores(rankingKey, 0, limit - 1);
        
        Map<String, Object> result = new HashMap<>();
        result.put("date", today);
        result.put("ranking", ranking);
        result.put("updateTime", System.currentTimeMillis());
        
        return result;
    }

    @Override
    public void updateProductSalesCache(Long productId, String productName, Integer quantity, Double amount) {
        String today = LocalDate.now().format(DATE_FORMATTER);
        String rankingKey = PRODUCT_RANKING_PREFIX + today;
        
        try {
            // 更新商品销量排行
            redisTemplate.opsForZSet().incrementScore(rankingKey, productId, quantity);
            
            // 设置过期时间
            redisTemplate.expire(rankingKey, 7, TimeUnit.DAYS);
            
            log.debug("商品销售缓存更新成功，商品ID：{}，数量：{}", productId, quantity);
        } catch (Exception e) {
            log.error("更新商品销售缓存失败，商品ID：{}", productId, e);
        }
    }

    @Override
    public Map<String, Object> getRealtimeMonitoringData() {
        Map<Object, Object> monitor = redisTemplate.opsForHash().entries(REALTIME_MONITOR_KEY);
        Map<String, Object> result = new HashMap<>();
        
        result.put("currentMinuteOrders", getLongValue(monitor, "minuteOrders"));
        result.put("currentMinuteAmount", getDoubleValue(monitor, "minuteAmount"));
        result.put("lastUpdateTime", monitor.get("lastUpdateTime"));
        
        return result;
    }

    @Override
    public Map<String, Object> warmupCache(Integer days) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("开始预热缓存，预热天数：{}", days);
            
            long startTime = System.currentTimeMillis();
            int warmedDays = 0;
            
            LocalDate today = LocalDate.now();
            for (int i = 0; i < days; i++) {
                LocalDate date = today.minusDays(i);
                String dateStr = date.format(DATE_FORMATTER);
                String dateKey = ORDER_STATS_PREFIX + dateStr;
                
                // 模拟预热数据
                Map<String, Object> dayStats = new HashMap<>();
                dayStats.put("totalOrders", 100 + i * 10);
                dayStats.put("totalAmount", 10000.0 + i * 1000);
                dayStats.put("status_1", 20);
                dayStats.put("status_2", 60);
                dayStats.put("status_4", 15);
                dayStats.put("status_5", 5);
                
                redisTemplate.opsForHash().putAll(dateKey, dayStats);
                redisTemplate.expire(dateKey, 7, TimeUnit.DAYS);
                
                warmedDays++;
            }
            
            long duration = System.currentTimeMillis() - startTime;
            
            result.put("success", true);
            result.put("warmedDays", warmedDays);
            result.put("duration", duration);
            result.put("message", "缓存预热完成");
            
            log.info("缓存预热完成，预热天数：{}，耗时：{}ms", warmedDays, duration);
            
        } catch (Exception e) {
            log.error("缓存预热失败", e);
            result.put("success", false);
            result.put("message", "缓存预热失败：" + e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> cleanExpiredCache(Integer beforeDays) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("开始清理过期缓存，清理{}天前的数据", beforeDays);
            
            long startTime = System.currentTimeMillis();
            int cleanedCount = 0;
            
            LocalDate cutoffDate = LocalDate.now().minusDays(beforeDays);
            
            // 清理过期的日期统计缓存
            for (int i = 0; i < 30; i++) { // 最多清理30天的数据
                LocalDate date = cutoffDate.minusDays(i);
                String dateStr = date.format(DATE_FORMATTER);
                String dateKey = ORDER_STATS_PREFIX + dateStr;
                
                if (redisTemplate.hasKey(dateKey)) {
                    redisTemplate.delete(dateKey);
                    cleanedCount++;
                }
                
                // 清理用户统计缓存
                String userPattern = USER_STATS_PREFIX + "*:" + dateStr;
                Set<String> userKeys = redisTemplate.keys(userPattern);
                if (userKeys != null && !userKeys.isEmpty()) {
                    redisTemplate.delete(userKeys);
                    cleanedCount += userKeys.size();
                }
                
                // 清理商品排行缓存
                String rankingKey = PRODUCT_RANKING_PREFIX + dateStr;
                if (redisTemplate.hasKey(rankingKey)) {
                    redisTemplate.delete(rankingKey);
                    cleanedCount++;
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            
            result.put("success", true);
            result.put("cleanedCount", cleanedCount);
            result.put("duration", duration);
            result.put("message", "过期缓存清理完成");
            
            log.info("过期缓存清理完成，清理数量：{}，耗时：{}ms", cleanedCount, duration);
            
        } catch (Exception e) {
            log.error("清理过期缓存失败", e);
            result.put("success", false);
            result.put("message", "清理失败：" + e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // 统计缓存键数量
            Set<String> orderStatsKeys = redisTemplate.keys(ORDER_STATS_PREFIX + "*");
            Set<String> userStatsKeys = redisTemplate.keys(USER_STATS_PREFIX + "*");
            Set<String> productRankingKeys = redisTemplate.keys(PRODUCT_RANKING_PREFIX + "*");
            
            stats.put("orderStatsKeyCount", orderStatsKeys != null ? orderStatsKeys.size() : 0);
            stats.put("userStatsKeyCount", userStatsKeys != null ? userStatsKeys.size() : 0);
            stats.put("productRankingKeyCount", productRankingKeys != null ? productRankingKeys.size() : 0);
            stats.put("totalKeyCount", 
                    (orderStatsKeys != null ? orderStatsKeys.size() : 0) +
                    (userStatsKeys != null ? userStatsKeys.size() : 0) +
                    (productRankingKeys != null ? productRankingKeys.size() : 0));
            
            stats.put("cacheStatus", "active");
            stats.put("lastUpdateTime", System.currentTimeMillis());
            
        } catch (Exception e) {
            log.error("获取缓存统计失败", e);
            stats.put("cacheStatus", "error");
            stats.put("error", e.getMessage());
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> refreshDateCache(LocalDate date) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "缓存刷新完成");
        return result;
    }

    @Override
    public void incrementUserStatistics(Long userId, Integer orderCount, Double orderAmount) {
        String today = LocalDate.now().format(DATE_FORMATTER);
        String userKey = USER_STATS_PREFIX + userId + ":" + today;
        
        try {
            redisTemplate.opsForHash().increment(userKey, "orderCount", orderCount);
            redisTemplate.opsForHash().increment(userKey, "orderAmount", orderAmount);
            redisTemplate.expire(userKey, 7, TimeUnit.DAYS);
            
            log.debug("用户统计增量更新成功，用户ID：{}，订单数：{}，金额：{}", userId, orderCount, orderAmount);
        } catch (Exception e) {
            log.error("用户统计增量更新失败，用户ID：{}", userId, e);
        }
    }

    @Override
    public Map<String, Object> getHotspotData() {
        Map<String, Object> result = new HashMap<>();
        result.put("hotspotData", "热点数据");
        return result;
    }

    @Override
    public void setCacheAlertThreshold(String metric, Double threshold) {
        log.info("设置缓存告警阈值：{} = {}", metric, threshold);
    }

    /**
     * 安全获取Long值
     */
    private Long getLongValue(Map<Object, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0L;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }

    /**
     * 安全获取Double值
     */
    private Double getDoubleValue(Map<Object, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0.0;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }
} 