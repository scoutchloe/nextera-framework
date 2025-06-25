package com.nextera.order.service;

import com.nextera.order.entity.Order;
import com.nextera.order.entity.OrderItem;

import java.util.List;
import java.util.Map;

/**
 * 订单数据同步服务接口
 * 
 * @author Nextera Framework
 * @since 1.0.0
 */
public interface OrderDataSyncService {

    /**
     * 同步订单创建事件
     * 
     * @param order 订单实体
     * @param orderItems 订单项列表
     */
    void syncOrderCreated(Order order, List<OrderItem> orderItems);

    /**
     * 同步订单更新事件
     * 
     * @param order 订单实体
     * @param orderItems 订单项列表
     */
    void syncOrderUpdated(Order order, List<OrderItem> orderItems);

    /**
     * 同步订单状态变更事件
     * 
     * @param orderId 订单ID
     * @param oldStatus 原状态
     * @param newStatus 新状态
     * @param orderAmount 订单金额
     */
    void syncOrderStatusChanged(Long orderId, Integer oldStatus, Integer newStatus, Double orderAmount);

    /**
     * 同步订单删除事件
     * 
     * @param orderId 订单ID
     */
    void syncOrderDeleted(Long orderId);

    /**
     * 批量同步历史订单数据
     * 
     * @param batchSize 批次大小
     * @param startId 起始ID
     * @param endId 结束ID
     * @return 同步结果
     */
    Map<String, Object> batchSyncHistoryOrders(Integer batchSize, Long startId, Long endId);

    /**
     * 全量重建索引
     * 
     * @return 重建结果
     */
    Map<String, Object> rebuildAllIndexes();

    /**
     * 检查数据一致性
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 一致性检查结果
     */
    Map<String, Object> checkDataConsistency(String startDate, String endDate);

    /**
     * 修复数据不一致
     * 
     * @param repairType 修复类型
     * @param targetDate 目标日期
     * @return 修复结果
     */
    Map<String, Object> repairDataInconsistency(String repairType, String targetDate);
} 