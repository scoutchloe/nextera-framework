package com.nextera.managenextera.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端订单查询传输对象
 *
 * @author nextera
 * @since 2025-01-01
 */
@Data
@Schema(description = "管理端订单信息")
public class OrderManageDTO {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "订单总金额")
    private BigDecimal totalAmount;

    @Schema(description = "订单状态")
    private Integer status;

    @Schema(description = "订单状态描述")
    private String statusDescription;

    @Schema(description = "支付方式")
    private Integer paymentMethod;

    @Schema(description = "支付方式描述")
    private String paymentMethodDescription;

    @Schema(description = "订单备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    @Schema(description = "订单明细列表")
    private List<OrderItemDTO> orderItems;

    /**
     * 订单明细DTO
     */
    @Data
    @Schema(description = "订单明细信息")
    public static class OrderItemDTO {

        @Schema(description = "明细ID")
        private Long id;

        @Schema(description = "订单ID")
        private Long orderId;

        @Schema(description = "商品ID")
        private Long productId;

        @Schema(description = "商品名称")
        private String productName;

        @Schema(description = "商品单价")
        private BigDecimal productPrice;

        @Schema(description = "购买数量")
        private Integer quantity;

        @Schema(description = "小计金额")
        private BigDecimal totalPrice;

        @Schema(description = "创建时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdTime;
    }
} 