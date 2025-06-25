package com.nextera.orderapi.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单响应DTO
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
@Data
public class OrderResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 订单状态
     */
    private Integer status;

    /**
     * 订单状态描述
     */
    private String statusDescription;

    /**
     * 支付方式
     */
    private Integer paymentMethod;

    /**
     * 支付方式描述
     */
    private String paymentMethodDescription;

    /**
     * 订单备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 订单明细列表
     */
    private List<OrderItemResponse> items;

    /**
     * 订单明细响应DTO
     */
    @Data
    public static class OrderItemResponse implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 明细ID
         */
        private Long id;

        /**
         * 订单ID
         */
        private Long orderId;

        /**
         * 商品ID
         */
        private Long productId;

        /**
         * 商品名称
         */
        private String productName;

        /**
         * 商品单价
         */
        private BigDecimal productPrice;

        /**
         * 购买数量
         */
        private Integer quantity;

        /**
         * 小计金额
         */
        private BigDecimal totalPrice;

        /**
         * 创建时间
         */
        private LocalDateTime createdTime;
    }
} 