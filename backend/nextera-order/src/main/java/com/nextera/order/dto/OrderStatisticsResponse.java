package com.nextera.order.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 订单统计响应DTO
 * 
 * @author Nextera Framework
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class OrderStatisticsResponse {

    /**
     * 统计日期范围开始
     */
    private LocalDate startDate;

    /**
     * 统计日期范围结束
     */
    private LocalDate endDate;

    /**
     * 订单总数
     */
    private Long totalOrders;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 平均订单金额
     */
    private BigDecimal avgOrderAmount;

    /**
     * 已支付订单数
     */
    private Long paidOrders;

    /**
     * 已取消订单数
     */
    private Long cancelledOrders;

    /**
     * 待支付订单数
     */
    private Long pendingOrders;

    /**
     * 订单支付率
     */
    private Double paymentRate;

    /**
     * 订单取消率
     */
    private Double cancellationRate;

    /**
     * 用户数（去重）
     */
    private Long uniqueUsers;

    /**
     * 人均订单数
     */
    private Double avgOrdersPerUser;

    /**
     * 商品总数量
     */
    private Long totalProductQuantity;
} 