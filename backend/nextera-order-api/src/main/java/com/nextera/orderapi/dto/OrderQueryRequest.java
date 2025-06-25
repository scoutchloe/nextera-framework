package com.nextera.orderapi.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单查询请求DTO
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
@Data
public class OrderQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页
     */
    private Integer current = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 订单状态
     */
    private Integer status;

    /**
     * 支付方式
     */
    private Integer paymentMethod;

    /**
     * 创建时间-开始
     */
    private LocalDateTime createdTimeStart;

    /**
     * 创建时间-结束
     */
    private LocalDateTime createdTimeEnd;
} 