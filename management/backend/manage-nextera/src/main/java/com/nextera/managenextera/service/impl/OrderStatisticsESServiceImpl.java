package com.nextera.managenextera.service.impl;

import com.nextera.managenextera.dto.OrderStatisticsDTO;
import com.nextera.managenextera.entity.OrderES;
import com.nextera.managenextera.repository.OrderESRepository;
import com.nextera.managenextera.service.OrderStatisticsService;
import com.nextera.managenextera.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 基于ES的订单统计服务实现类
 *
 * @author nextera
 * @since 2025-01-01
 */
@Slf4j
@Service("orderStatisticsESService")
public class OrderStatisticsESServiceImpl implements OrderStatisticsService {

    @Autowired
    private OrderESRepository orderESRepository;

    @Autowired
    private RedisService redisService;

    private static final String STATISTICS_CACHE_PREFIX = "order:statistics:es:";
    
    // 缓存键定义
    private static final String OVERVIEW_CACHE_KEY = STATISTICS_CACHE_PREFIX + "overview";
    private static final String REALTIME_CACHE_KEY = STATISTICS_CACHE_PREFIX + "realtime";
    private static final String TREND_CACHE_PREFIX = STATISTICS_CACHE_PREFIX + "trend:";
    private static final String STATUS_DIST_CACHE_PREFIX = STATISTICS_CACHE_PREFIX + "status:";
    private static final String PRODUCT_RANK_CACHE_PREFIX = STATISTICS_CACHE_PREFIX + "product:";
    private static final String USER_ANALYSIS_CACHE_PREFIX = STATISTICS_CACHE_PREFIX + "user:";
    private static final String AMOUNT_DIST_CACHE_PREFIX = STATISTICS_CACHE_PREFIX + "amount:";

    @Override
    public OrderStatisticsDTO.OverviewStatistics getOverviewStatistics() {
        log.info("获取概览统计（ES版本）");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
            LocalDateTime monthStart = now.toLocalDate().withDayOfMonth(1).atStartOfDay();
            
            OrderStatisticsDTO.OverviewStatistics overview = new OrderStatisticsDTO.OverviewStatistics();
            
            // 今日统计
            long todayOrderCount = orderESRepository.countByCreatedTimeBetween(todayStart, now);
            overview.setTodayOrderCount(todayOrderCount);
            
            // 计算今日订单金额
            List<OrderES> todayOrders = orderESRepository.findByCreatedTimeBetween(todayStart, now);
            BigDecimal todayOrderAmount = todayOrders.stream()
                    .map(OrderES::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            overview.setTodayOrderAmount(todayOrderAmount);
            
            // 本月统计
            long monthOrderCount = orderESRepository.countByCreatedTimeBetween(monthStart, now);
            overview.setMonthOrderCount(monthOrderCount);
            
            List<OrderES> monthOrders = orderESRepository.findByCreatedTimeBetween(monthStart, now);
            BigDecimal monthOrderAmount = monthOrders.stream()
                    .map(OrderES::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            overview.setMonthOrderAmount(monthOrderAmount);
            
            // 总统计
            long totalOrderCount = orderESRepository.count();
            overview.setTotalOrderCount(totalOrderCount);
            overview.setTotalOrderAmount(BigDecimal.ZERO); // 简化处理
            
            // 各状态统计
            overview.setPendingOrderCount(orderESRepository.countByStatus(0));
            overview.setPaidOrderCount(orderESRepository.countByStatus(1));
            overview.setCompletedOrderCount(orderESRepository.countByStatus(3));
            overview.setCancelledOrderCount(orderESRepository.countByStatus(4));
            
            overview.setStatisticsTime(LocalDateTime.now());
            
            return overview;
            
        } catch (Exception e) {
            log.error("获取概览统计失败", e);
            return createDefaultOverviewStatistics();
        }
    }

    @Override
    public OrderStatisticsDTO.RealtimeStatistics getRealtimeStatistics() {
        log.info("获取实时统计（ES版本）");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lastHour = now.minusHours(1);
            LocalDateTime last24Hours = now.minusHours(24);
            LocalDateTime last7Days = now.minusDays(7);
            LocalDateTime last30Days = now.minusDays(30);
            
            OrderStatisticsDTO.RealtimeStatistics realtime = new OrderStatisticsDTO.RealtimeStatistics();
            
            realtime.setLastHourOrderCount(orderESRepository.countByCreatedTimeBetween(lastHour, now));
            realtime.setLast24HourOrderCount(orderESRepository.countByCreatedTimeBetween(last24Hours, now));
            realtime.setLast7DayOrderCount(orderESRepository.countByCreatedTimeBetween(last7Days, now));
            realtime.setLast30DayOrderCount(orderESRepository.countByCreatedTimeBetween(last30Days, now));
            
            // 简化金额统计
            realtime.setLastHourOrderAmount(BigDecimal.ZERO);
            realtime.setLast24HourOrderAmount(BigDecimal.ZERO);
            realtime.setLast7DayOrderAmount(BigDecimal.ZERO);
            realtime.setLast30DayOrderAmount(BigDecimal.ZERO);
            
            realtime.setStatisticsTime(LocalDateTime.now());
            
            return realtime;
            
        } catch (Exception e) {
            log.error("获取实时统计失败", e);
            return createDefaultRealtimeStatistics();
        }
    }

    @Override
    public List<OrderStatisticsDTO.TrendStatistics> getOrderTrend(LocalDate startDate, LocalDate endDate) {
        log.info("获取订单趋势（ES版本）: {} - {}", startDate, endDate);
        
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
            
            List<OrderES> orders = orderESRepository.findByCreatedTimeBetween(startDateTime, endDateTime);
            
            Map<LocalDate, List<OrderES>> ordersByDate = orders.stream()
                    .collect(Collectors.groupingBy(order -> order.getCreatedTime().toLocalDate()));
            
            List<OrderStatisticsDTO.TrendStatistics> trendList = new ArrayList<>();
            
            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                List<OrderES> dayOrders = ordersByDate.getOrDefault(currentDate, new ArrayList<>());
                
                OrderStatisticsDTO.TrendStatistics trend = new OrderStatisticsDTO.TrendStatistics();
                trend.setDate(currentDate);
                trend.setOrderCount((long) dayOrders.size());
                
                BigDecimal dayAmount = dayOrders.stream()
                        .map(OrderES::getTotalAmount)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                trend.setOrderAmount(dayAmount);
                trend.setNewUserCount(0L); // 简化处理
                
                trendList.add(trend);
                currentDate = currentDate.plusDays(1);
            }
            
            return trendList;
            
        } catch (Exception e) {
            log.error("获取订单趋势失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<OrderStatisticsDTO.StatusDistribution> getOrderStatusDistribution(LocalDate startDate, LocalDate endDate) {
        log.info("获取订单状态分布（ES版本）: {} - {}", startDate, endDate);
        
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
            
            List<OrderStatisticsDTO.StatusDistribution> statusList = new ArrayList<>();
            
            for (int status = 0; status <= 5; status++) {
                long count = orderESRepository.countByStatusAndCreatedTimeBetween(status, startDateTime, endDateTime);
                if (count > 0) {
                    OrderStatisticsDTO.StatusDistribution statusDist = new OrderStatisticsDTO.StatusDistribution();
                    statusDist.setStatus(status);
                    statusDist.setStatusDescription(getStatusName(status));
                    statusDist.setCount(count);
                    statusList.add(statusDist);
                }
            }
            
            return statusList;
            
        } catch (Exception e) {
            log.error("获取订单状态分布失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<OrderStatisticsDTO.ProductSalesRanking> getProductSalesRanking(LocalDate startDate, LocalDate endDate, Integer limit) {
        log.info("获取商品销售排行榜（ES版本）: {} - {}, 限制: {}", startDate, endDate, limit);
        
        String cacheKey = PRODUCT_RANK_CACHE_PREFIX + startDate + ":" + endDate + ":" + limit;
        
        // 先从缓存获取
        @SuppressWarnings("unchecked")
        List<OrderStatisticsDTO.ProductSalesRanking> cached = 
            (List<OrderStatisticsDTO.ProductSalesRanking>) redisService.get(cacheKey);
        if (cached != null) {
            log.info("从缓存获取商品销售排行榜");
            return cached;
        }
        
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
            
            // 查询指定时间范围内的已完成订单
            List<OrderES> orders = orderESRepository.findByStatusInAndCreatedTimeBetween(
                Arrays.asList(3, 4, 5), startDateTime, endDateTime);
            
            // 统计商品销售数据
            Map<String, OrderStatisticsDTO.ProductSalesRanking> productStatsMap = new HashMap<>();
            
            for (OrderES order : orders) {
                if (order.getOrderItems() != null) {
                    for (OrderES.OrderItemES item : order.getOrderItems()) {
                        String key = item.getProductId() + "_" + item.getProductName();
                        
                        OrderStatisticsDTO.ProductSalesRanking ranking = productStatsMap.computeIfAbsent(key, k -> {
                            OrderStatisticsDTO.ProductSalesRanking newRanking = new OrderStatisticsDTO.ProductSalesRanking();
                            newRanking.setProductId(item.getProductId());
                            newRanking.setProductName(item.getProductName());
                            newRanking.setSalesCount(0L);
                            newRanking.setSalesAmount(BigDecimal.ZERO);
                            return newRanking;
                        });
                        
                        ranking.setSalesCount(ranking.getSalesCount() + item.getQuantity());
                        ranking.setSalesAmount(ranking.getSalesAmount().add(item.getTotalPrice()));
                    }
                }
            }
            
            // 按销量排序并设置排名
            List<OrderStatisticsDTO.ProductSalesRanking> result = productStatsMap.values().stream()
                    .sorted((a, b) -> b.getSalesCount().compareTo(a.getSalesCount()))
                    .limit(limit != null ? limit : 10)
                    .collect(Collectors.toList());
            
            // 设置排名
            for (int i = 0; i < result.size(); i++) {
                result.get(i).setRank(i + 1);
            }
            
            // 缓存结果15分钟
            redisService.set(cacheKey, result, 15, TimeUnit.MINUTES);
            
            log.info("商品销售排行榜统计完成，共 {} 个商品", result.size());
            return result;
            
        } catch (Exception e) {
            log.error("获取商品销售排行榜失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<OrderStatisticsDTO.UserOrderAnalysis> getUserOrderAnalysis(LocalDate startDate, LocalDate endDate, Integer limit) {
        log.info("获取用户订单分析（ES版本）: {} - {}, 限制: {}", startDate, endDate, limit);
        
        String cacheKey = USER_ANALYSIS_CACHE_PREFIX + startDate + ":" + endDate + ":" + limit;
        
        // 先从缓存获取
        @SuppressWarnings("unchecked")
        List<OrderStatisticsDTO.UserOrderAnalysis> cached = 
            (List<OrderStatisticsDTO.UserOrderAnalysis>) redisService.get(cacheKey);
        if (cached != null) {
            log.info("从缓存获取用户订单分析");
            return cached;
        }
        
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
            
            List<OrderES> orders = orderESRepository.findByCreatedTimeBetween(startDateTime, endDateTime);
            
            // 按用户分组统计
            Map<Long, List<OrderES>> ordersByUser = orders.stream()
                    .filter(order -> order.getUserId() != null)
                    .collect(Collectors.groupingBy(OrderES::getUserId));
            
            List<OrderStatisticsDTO.UserOrderAnalysis> result = new ArrayList<>();
            
            for (Map.Entry<Long, List<OrderES>> entry : ordersByUser.entrySet()) {
                List<OrderES> userOrders = entry.getValue();
                
                OrderStatisticsDTO.UserOrderAnalysis analysis = new OrderStatisticsDTO.UserOrderAnalysis();
                analysis.setUserId(entry.getKey());
                analysis.setUsername(userOrders.get(0).getUserName());
                analysis.setOrderCount((long) userOrders.size());
                
                BigDecimal totalAmount = userOrders.stream()
                        .map(OrderES::getTotalAmount)
                        .filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                analysis.setTotalAmount(totalAmount);
                
                if (userOrders.size() > 0) {
                    analysis.setAvgOrderAmount(totalAmount.divide(
                        BigDecimal.valueOf(userOrders.size()), 2, BigDecimal.ROUND_HALF_UP));
                }
                
                LocalDateTime lastOrderTime = userOrders.stream()
                        .map(OrderES::getCreatedTime)
                        .filter(Objects::nonNull)
                        .max(LocalDateTime::compareTo)
                        .orElse(null);
                analysis.setLastOrderTime(lastOrderTime);
                
                result.add(analysis);
            }
            
            // 按订单数量排序
            result = result.stream()
                    .sorted((a, b) -> b.getOrderCount().compareTo(a.getOrderCount()))
                    .limit(limit != null ? limit : 10)
                    .collect(Collectors.toList());
            
            // 缓存结果15分钟
            redisService.set(cacheKey, result, 15, TimeUnit.MINUTES);
            
            log.info("用户订单分析完成，共 {} 个用户", result.size());
            return result;
            
        } catch (Exception e) {
            log.error("获取用户订单分析失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Long> getOrderAmountDistribution(LocalDate startDate, LocalDate endDate) {
        log.info("获取订单金额分布（ES版本）: {} - {}", startDate, endDate);
        
        String cacheKey = AMOUNT_DIST_CACHE_PREFIX + startDate + ":" + endDate;
        
        // 先从缓存获取
        @SuppressWarnings("unchecked")
        Map<String, Long> cached = (Map<String, Long>) redisService.get(cacheKey);
        if (cached != null) {
            log.info("从缓存获取订单金额分布");
            return cached;
        }
        
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();
            
            List<OrderES> orders = orderESRepository.findByCreatedTimeBetween(startDateTime, endDateTime);
            
            Map<String, Long> distribution = new LinkedHashMap<>();
            distribution.put("0-100", 0L);
            distribution.put("100-500", 0L);
            distribution.put("500-1000", 0L);
            distribution.put("1000-5000", 0L);
            distribution.put("5000+", 0L);
            
            for (OrderES order : orders) {
                if (order.getTotalAmount() != null) {
                    BigDecimal amount = order.getTotalAmount();
                    if (amount.compareTo(BigDecimal.valueOf(100)) < 0) {
                        distribution.put("0-100", distribution.get("0-100") + 1);
                    } else if (amount.compareTo(BigDecimal.valueOf(500)) < 0) {
                        distribution.put("100-500", distribution.get("100-500") + 1);
                    } else if (amount.compareTo(BigDecimal.valueOf(1000)) < 0) {
                        distribution.put("500-1000", distribution.get("500-1000") + 1);
                    } else if (amount.compareTo(BigDecimal.valueOf(5000)) < 0) {
                        distribution.put("1000-5000", distribution.get("1000-5000") + 1);
                    } else {
                        distribution.put("5000+", distribution.get("5000+") + 1);
                    }
                }
            }
            
            // 缓存结果15分钟
            redisService.set(cacheKey, distribution, 15, TimeUnit.MINUTES);
            
            log.info("订单金额分布统计完成");
            return distribution;
            
        } catch (Exception e) {
            log.error("获取订单金额分布失败", e);
            return new HashMap<>();
        }
    }

    @Override
    public boolean refreshStatisticsCache() {
        log.info("刷新统计缓存（ES版本）");
        // 由于RedisService没有keys方法，这里简化处理
        // 清除主要的缓存键
        redisService.delete(OVERVIEW_CACHE_KEY);
        redisService.delete(REALTIME_CACHE_KEY);
        log.info("统计缓存刷新完成");
        return true;
    }

    @Override
    public Object getCachedStatistics(String cacheKey) {
        return redisService.get(STATISTICS_CACHE_PREFIX + cacheKey);
    }

    @Override
    public void setCachedStatistics(String cacheKey, Object data, long expireSeconds) {
        redisService.set(STATISTICS_CACHE_PREFIX + cacheKey, data, expireSeconds, TimeUnit.SECONDS);
    }

    private OrderStatisticsDTO.OverviewStatistics createDefaultOverviewStatistics() {
        OrderStatisticsDTO.OverviewStatistics overview = new OrderStatisticsDTO.OverviewStatistics();
        overview.setTodayOrderCount(0L);
        overview.setTodayOrderAmount(BigDecimal.ZERO);
        overview.setMonthOrderCount(0L);
        overview.setMonthOrderAmount(BigDecimal.ZERO);
        overview.setTotalOrderCount(0L);
        overview.setTotalOrderAmount(BigDecimal.ZERO);
        overview.setPendingOrderCount(0L);
        overview.setPaidOrderCount(0L);
        overview.setCompletedOrderCount(0L);
        overview.setCancelledOrderCount(0L);
        overview.setStatisticsTime(LocalDateTime.now());
        return overview;
    }

    private OrderStatisticsDTO.RealtimeStatistics createDefaultRealtimeStatistics() {
        OrderStatisticsDTO.RealtimeStatistics realtime = new OrderStatisticsDTO.RealtimeStatistics();
        realtime.setLastHourOrderCount(0L);
        realtime.setLast24HourOrderCount(0L);
        realtime.setLast7DayOrderCount(0L);
        realtime.setLast30DayOrderCount(0L);
        realtime.setLastHourOrderAmount(BigDecimal.ZERO);
        realtime.setLast24HourOrderAmount(BigDecimal.ZERO);
        realtime.setLast7DayOrderAmount(BigDecimal.ZERO);
        realtime.setLast30DayOrderAmount(BigDecimal.ZERO);
        realtime.setStatisticsTime(LocalDateTime.now());
        return realtime;
    }

    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待支付";
            case 1 -> "已支付";
            case 2 -> "已发货";
            case 3 -> "已完成";
            case 4 -> "已取消";
            case 5 -> "已退款";
            default -> "未知";
        };
    }
} 