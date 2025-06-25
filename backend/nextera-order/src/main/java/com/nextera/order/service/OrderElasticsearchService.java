package com.nextera.order.service;

import com.nextera.order.document.OrderDocument;
import com.nextera.order.dto.OrderAnalysisResponse;
import com.nextera.order.dto.OrderStatisticsResponse;
import com.nextera.order.entity.Order;
import com.nextera.order.entity.OrderItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单ElasticSearch聚合服务接口
 * 
 * @author Nextera Framework
 * @since 1.0.0
 */
public interface OrderElasticsearchService {

    /**
     * 同步订单数据到ElasticSearch
     * 
     * @param order 订单实体
     * @param orderItems 订单项列表
     */
    void syncOrderToElasticsearch(Order order, List<OrderItem> orderItems);

    /**
     * 批量同步订单数据
     * 
     * @param orders 订单列表
     */
    void batchSyncOrders(List<Order> orders);

    /**
     * 删除订单文档
     * 
     * @param orderId 订单ID
     */
    void deleteOrderDocument(Long orderId);

    /**
     * 根据订单号查询
     * 
     * @param orderNo 订单号
     * @return 订单文档
     */
    OrderDocument findByOrderNo(String orderNo);

    /**
     * 根据用户ID查询订单列表
     * 
     * @param userId 用户ID
     * @return 订单文档列表
     */
    List<OrderDocument> findOrdersByUserId(Long userId);

    /**
     * ElasticSearch聚合统计
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计结果
     */
    OrderStatisticsResponse getOrderStatisticsByES(LocalDate startDate, LocalDate endDate);

    /**
     * 用户订单统计
     * 
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 用户统计结果
     */
    OrderStatisticsResponse getUserOrderStatisticsByES(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 订单趋势分析（按日期聚合）
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 趋势分析结果
     */
    List<OrderAnalysisResponse> getOrderTrendByES(LocalDate startDate, LocalDate endDate);

    /**
     * 商品销售排行榜
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param limit 返回条数
     * @return 商品销售排行
     */
    List<Map<String, Object>> getProductSalesRankingByES(LocalDate startDate, LocalDate endDate, Integer limit);

    /**
     * 按状态统计订单
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 状态统计结果
     */
    Map<String, Long> getOrderStatusStatisticsByES(LocalDate startDate, LocalDate endDate);

    /**
     * 订单金额分布统计
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 金额分布统计
     */
    Map<String, Long> getOrderAmountDistributionByES(LocalDate startDate, LocalDate endDate);

    /**
     * 高级搜索（支持多条件）
     * 
     * @param searchParams 搜索参数
     * @return 搜索结果
     */
    List<OrderDocument> advancedSearch(Map<String, Object> searchParams);

    /**
     * 重建索引（数据迁移）
     * 
     * @param batchSize 批处理大小
     * @return 处理结果
     */
    Map<String, Object> rebuildIndex(Integer batchSize);
} 