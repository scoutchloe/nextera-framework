package com.nextera.order.service.impl;

import com.nextera.order.document.OrderDocument;
import com.nextera.order.dto.OrderAnalysisResponse;
import com.nextera.order.dto.OrderStatisticsResponse;
import com.nextera.order.entity.Order;
import com.nextera.order.entity.OrderItem;
import com.nextera.order.service.OrderElasticsearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * 订单ElasticSearch服务备用实现类（当ES不可用时使用）
 * 
 * @author Nextera Framework
 * @since 1.0.0
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "false", matchIfMissing = true)
public class OrderElasticsearchServiceFallbackImpl implements OrderElasticsearchService {

    @Override
    public void syncOrderToElasticsearch(Order order, List<OrderItem> orderItems) {
        log.debug("ElasticSearch未启用，跳过订单同步，订单ID：{}", order.getId());
    }

    @Override
    public void batchSyncOrders(List<Order> orders) {
        log.debug("ElasticSearch未启用，跳过批量订单同步，数量：{}", orders.size());
    }

    @Override
    public void deleteOrderDocument(Long orderId) {
        log.debug("ElasticSearch未启用，跳过删除订单文档，订单ID：{}", orderId);
    }

    @Override
    public OrderDocument findByOrderNo(String orderNo) {
        log.debug("ElasticSearch未启用，无法查询订单号：{}", orderNo);
        return null;
    }

    @Override
    public List<OrderDocument> findOrdersByUserId(Long userId) {
        log.debug("ElasticSearch未启用，无法查询用户订单，用户ID：{}", userId);
        return Collections.emptyList();
    }

    @Override
    public OrderStatisticsResponse getOrderStatisticsByES(LocalDate startDate, LocalDate endDate) {
        log.debug("ElasticSearch未启用，返回空统计数据");
        return new OrderStatisticsResponse()
                .setStartDate(startDate)
                .setEndDate(endDate)
                .setTotalOrders(0L)
                .setTotalAmount(BigDecimal.ZERO);
    }

    @Override
    public OrderStatisticsResponse getUserOrderStatisticsByES(Long userId, LocalDate startDate, LocalDate endDate) {
        log.debug("ElasticSearch未启用，返回空用户统计数据，用户ID：{}", userId);
        return new OrderStatisticsResponse()
                .setStartDate(startDate)
                .setEndDate(endDate)
                .setTotalOrders(0L)
                .setTotalAmount(BigDecimal.ZERO);
    }

    @Override
    public List<OrderAnalysisResponse> getOrderTrendByES(LocalDate startDate, LocalDate endDate) {
        log.debug("ElasticSearch未启用，返回空趋势数据");
        return Collections.emptyList();
    }

    @Override
    public List<Map<String, Object>> getProductSalesRankingByES(LocalDate startDate, LocalDate endDate, Integer limit) {
        log.debug("ElasticSearch未启用，返回空排行数据");
        return Collections.emptyList();
    }

    @Override
    public Map<String, Long> getOrderStatusStatisticsByES(LocalDate startDate, LocalDate endDate) {
        log.debug("ElasticSearch未启用，返回空状态统计");
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Long> getOrderAmountDistributionByES(LocalDate startDate, LocalDate endDate) {
        log.debug("ElasticSearch未启用，返回空金额分布");
        return Collections.emptyMap();
    }

    @Override
    public List<OrderDocument> advancedSearch(Map<String, Object> searchParams) {
        log.debug("ElasticSearch未启用，无法执行高级搜索");
        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> rebuildIndex(Integer batchSize) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "ElasticSearch未启用，无法重建索引");
        return result;
    }
} 