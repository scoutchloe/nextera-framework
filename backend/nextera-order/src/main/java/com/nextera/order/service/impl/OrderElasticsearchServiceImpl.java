package com.nextera.order.service.impl;

import com.nextera.order.document.OrderDocument;
import com.nextera.order.document.OrderItemDocument;
import com.nextera.order.dto.OrderAnalysisResponse;
import com.nextera.order.dto.OrderStatisticsResponse;
import com.nextera.order.entity.Order;
import com.nextera.order.entity.OrderItem;
import com.nextera.order.repository.OrderDocumentRepository;
import com.nextera.order.service.OrderElasticsearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单ElasticSearch聚合服务实现类
 * 
 * @author Nextera Framework
 * @since 1.0.0
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class OrderElasticsearchServiceImpl implements OrderElasticsearchService {

    @Autowired
    private OrderDocumentRepository orderDocumentRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Override
    public void syncOrderToElasticsearch(Order order, List<OrderItem> orderItems) {
        try {
            OrderDocument orderDocument = convertToDocument(order, orderItems);
            orderDocumentRepository.save(orderDocument);
            log.debug("订单同步到ElasticSearch成功，订单ID：{}", order.getId());
        } catch (Exception e) {
            log.error("订单同步到ElasticSearch失败，订单ID：{}", order.getId(), e);
        }
    }

    @Override
    public void batchSyncOrders(List<Order> orders) {
        try {
            List<OrderDocument> documents = orders.stream()
                    .map(order -> convertToDocument(order, Collections.emptyList()))
                    .collect(Collectors.toList());
            
            orderDocumentRepository.saveAll(documents);
            log.info("批量同步订单到ElasticSearch成功，数量：{}", orders.size());
        } catch (Exception e) {
            log.error("批量同步订单到ElasticSearch失败", e);
        }
    }

    @Override
    public void deleteOrderDocument(Long orderId) {
        try {
            orderDocumentRepository.deleteById(orderId);
            log.debug("删除ElasticSearch订单文档成功，订单ID：{}", orderId);
        } catch (Exception e) {
            log.error("删除ElasticSearch订单文档失败，订单ID：{}", orderId, e);
        }
    }

    @Override
    public OrderDocument findByOrderNo(String orderNo) {
        return orderDocumentRepository.findByOrderNo(orderNo);
    }

    @Override
    public List<OrderDocument> findOrdersByUserId(Long userId) {
        return orderDocumentRepository.findByUserId(userId);
    }

    @Override
    public OrderStatisticsResponse getOrderStatisticsByES(LocalDate startDate, LocalDate endDate) {
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            
            List<OrderDocument> orders = orderDocumentRepository.findAllByCreatedTimeBetween(startDateTime, endDateTime);
            
            return calculateStatistics(orders, startDate, endDate);
        } catch (Exception e) {
            log.error("ElasticSearch聚合统计失败", e);
            return new OrderStatisticsResponse()
                    .setStartDate(startDate)
                    .setEndDate(endDate)
                    .setTotalOrders(0L)
                    .setTotalAmount(BigDecimal.ZERO);
        }
    }

    @Override
    public OrderStatisticsResponse getUserOrderStatisticsByES(Long userId, LocalDate startDate, LocalDate endDate) {
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            
            List<OrderDocument> orders = orderDocumentRepository.findByUserIdAndCreatedTimeBetween(userId, startDateTime, endDateTime);
            
            return calculateStatistics(orders, startDate, endDate);
        } catch (Exception e) {
            log.error("用户订单ElasticSearch聚合统计失败，用户ID：{}", userId, e);
            return new OrderStatisticsResponse()
                    .setStartDate(startDate)
                    .setEndDate(endDate)
                    .setTotalOrders(0L)
                    .setTotalAmount(BigDecimal.ZERO);
        }
    }

    @Override
    public List<OrderAnalysisResponse> getOrderTrendByES(LocalDate startDate, LocalDate endDate) {
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            
            List<OrderDocument> orders = orderDocumentRepository.findAllByCreatedTimeBetween(startDateTime, endDateTime);
            
            // 按日期分组统计
            Map<LocalDate, List<OrderDocument>> dailyOrders = orders.stream()
                    .collect(Collectors.groupingBy(order -> order.getCreatedTime().toLocalDate()));
            
            return dailyOrders.entrySet().stream()
                    .map(entry -> {
                        LocalDate date = entry.getKey();
                        List<OrderDocument> dayOrders = entry.getValue();
                        
                        long totalOrders = dayOrders.size();
                        BigDecimal totalAmount = dayOrders.stream()
                                .map(OrderDocument::getTotalAmount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        
                        return new OrderAnalysisResponse()
                                .setDate(date)
                                .setOrderCount(totalOrders)
                                .setTotalAmount(totalAmount)
                                .setAvgAmount(totalOrders > 0 ? totalAmount.divide(BigDecimal.valueOf(totalOrders), 2, BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO);
                    })
                    .sorted(Comparator.comparing(OrderAnalysisResponse::getDate))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("订单趋势ElasticSearch分析失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Map<String, Object>> getProductSalesRankingByES(LocalDate startDate, LocalDate endDate, Integer limit) {
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            
            List<OrderDocument> orders = orderDocumentRepository.findAllByCreatedTimeBetween(startDateTime, endDateTime);
            
            // 统计商品销售数据
            Map<Long, Map<String, Object>> productSales = new HashMap<>();
            
            for (OrderDocument order : orders) {
                if (order.getOrderItems() != null) {
                    for (OrderItemDocument item : order.getOrderItems()) {
                        Long productId = item.getProductId();
                        productSales.computeIfAbsent(productId, k -> {
                            Map<String, Object> sales = new HashMap<>();
                            sales.put("productId", productId);
                            sales.put("productName", item.getProductName());
                            sales.put("totalQuantity", 0);
                            sales.put("totalAmount", BigDecimal.ZERO);
                            sales.put("orderCount", 0);
                            return sales;
                        });
                        
                        Map<String, Object> sales = productSales.get(productId);
                        sales.put("totalQuantity", (Integer) sales.get("totalQuantity") + item.getQuantity());
                        sales.put("totalAmount", ((BigDecimal) sales.get("totalAmount")).add(item.getTotalPrice()));
                        sales.put("orderCount", (Integer) sales.get("orderCount") + 1);
                    }
                }
            }
            
            return productSales.values().stream()
                    .sorted((a, b) -> ((Integer) b.get("totalQuantity")).compareTo((Integer) a.get("totalQuantity")))
                    .limit(limit)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("商品销售排行ElasticSearch分析失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public Map<String, Long> getOrderStatusStatisticsByES(LocalDate startDate, LocalDate endDate) {
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            
            List<OrderDocument> orders = orderDocumentRepository.findAllByCreatedTimeBetween(startDateTime, endDateTime);
            
            return orders.stream()
                    .collect(Collectors.groupingBy(
                            order -> "status_" + order.getStatus(),
                            Collectors.counting()
                    ));
        } catch (Exception e) {
            log.error("订单状态统计ElasticSearch分析失败", e);
            return Collections.emptyMap();
        }
    }

    @Override
    public Map<String, Long> getOrderAmountDistributionByES(LocalDate startDate, LocalDate endDate) {
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            
            List<OrderDocument> orders = orderDocumentRepository.findAllByCreatedTimeBetween(startDateTime, endDateTime);
            
            Map<String, Long> distribution = new HashMap<>();
            distribution.put("0-100", 0L);
            distribution.put("100-500", 0L);
            distribution.put("500-1000", 0L);
            distribution.put("1000-5000", 0L);
            distribution.put("5000+", 0L);
            
            for (OrderDocument order : orders) {
                double amount = order.getTotalAmount().doubleValue();
                if (amount < 100) {
                    distribution.put("0-100", distribution.get("0-100") + 1);
                } else if (amount < 500) {
                    distribution.put("100-500", distribution.get("100-500") + 1);
                } else if (amount < 1000) {
                    distribution.put("500-1000", distribution.get("500-1000") + 1);
                } else if (amount < 5000) {
                    distribution.put("1000-5000", distribution.get("1000-5000") + 1);
                } else {
                    distribution.put("5000+", distribution.get("5000+") + 1);
                }
            }
            
            return distribution;
        } catch (Exception e) {
            log.error("订单金额分布ElasticSearch分析失败", e);
            return Collections.emptyMap();
        }
    }

    @Override
    public List<OrderDocument> advancedSearch(Map<String, Object> searchParams) {
        try {
            // 简化实现，返回基础查询结果
            List<OrderDocument> documents = new ArrayList<>();
            orderDocumentRepository.findAll().forEach(documents::add);
            return documents.stream()
                    .limit(100)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("ElasticSearch高级搜索失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public Map<String, Object> rebuildIndex(Integer batchSize) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 删除现有索引
            elasticsearchOperations.indexOps(OrderDocument.class).delete();
            
            // 重新创建索引
            elasticsearchOperations.indexOps(OrderDocument.class).create();
            elasticsearchOperations.indexOps(OrderDocument.class).putMapping();
            
            result.put("success", true);
            result.put("message", "索引重建成功");
            result.put("timestamp", System.currentTimeMillis());
            
            log.info("ElasticSearch索引重建成功");
        } catch (Exception e) {
            log.error("ElasticSearch索引重建失败", e);
            result.put("success", false);
            result.put("message", "索引重建失败：" + e.getMessage());
        }
        
        return result;
    }

    /**
     * 转换Order实体为ElasticSearch文档
     */
    private OrderDocument convertToDocument(Order order, List<OrderItem> orderItems) {
        OrderDocument document = new OrderDocument();
        BeanUtils.copyProperties(order, document);
        
        if (orderItems != null && !orderItems.isEmpty()) {
            List<OrderItemDocument> itemDocuments = orderItems.stream()
                    .map(this::convertToItemDocument)
                    .collect(Collectors.toList());
            document.setOrderItems(itemDocuments);
        }
        
        return document;
    }

    /**
     * 转换OrderItem实体为文档
     */
    private OrderItemDocument convertToItemDocument(OrderItem orderItem) {
        OrderItemDocument itemDocument = new OrderItemDocument();
        BeanUtils.copyProperties(orderItem, itemDocument);
        return itemDocument;
    }

    /**
     * 计算统计数据
     */
    private OrderStatisticsResponse calculateStatistics(List<OrderDocument> orders, LocalDate startDate, LocalDate endDate) {
        long totalOrders = orders.size();
        BigDecimal totalAmount = orders.stream()
                .map(OrderDocument::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long paidOrders = orders.stream()
                .filter(order -> order.getStatus() == 2)
                .count();
        
        long cancelledOrders = orders.stream()
                .filter(order -> order.getStatus() == 5)
                .count();
        
        long pendingOrders = orders.stream()
                .filter(order -> order.getStatus() == 1)
                .count();
        
        BigDecimal avgAmount = totalOrders > 0 ? 
                totalAmount.divide(BigDecimal.valueOf(totalOrders), 2, BigDecimal.ROUND_HALF_UP) : 
                BigDecimal.ZERO;
        
        return new OrderStatisticsResponse()
                .setStartDate(startDate)
                .setEndDate(endDate)
                .setTotalOrders(totalOrders)
                .setTotalAmount(totalAmount)
                .setPaidOrders(paidOrders)
                .setCancelledOrders(cancelledOrders)
                .setPendingOrders(pendingOrders)
                .setAvgOrderAmount(avgAmount);
    }
} 