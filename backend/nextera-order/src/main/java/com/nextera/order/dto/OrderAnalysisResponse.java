package com.nextera.order.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 订单分析响应DTO
 * 
 * @author Nextera Framework
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class OrderAnalysisResponse {

    /**
     * 统计日期
     */
    private LocalDate date;

    /**
     * 订单数量
     */
    private Long orderCount;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 平均订单金额
     */
    private BigDecimal avgAmount;

    /**
     * 用户数量
     */
    private Long userCount;

    /**
     * 商品数量
     */
    private Long productCount;

    /**
     * 维度标识（日期、用户、商品等）
     */
    private String dimension;

    /**
     * 维度值
     */
    private String dimensionValue;
} 