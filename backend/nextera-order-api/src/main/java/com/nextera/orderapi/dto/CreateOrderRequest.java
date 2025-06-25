package com.nextera.orderapi.dto;

import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 创建订单请求DTO
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
@Data
public class CreateOrderRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 支付方式(1:微信,2:支付宝,3:银行卡)
     */
    private Integer paymentMethod;

    /**
     * 订单备注
     */
    private String remark;

    /**
     * 订单明细列表
     */
    @Valid
    @NotEmpty(message = "订单明细不能为空")
    private List<OrderItemDto> items;

    /**
     * 订单明细DTO
     */
    @Data
    public static class OrderItemDto implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 商品ID
         */
        @NotNull(message = "商品ID不能为空")
        private Long productId;

        /**
         * 商品名称
         */
        @NotNull(message = "商品名称不能为空")
        private String productName;

        /**
         * 商品单价
         */
        @NotNull(message = "商品单价不能为空")
        @DecimalMin(value = "0.01", message = "商品单价必须大于0")
        private BigDecimal productPrice;

        /**
         * 购买数量
         */
        @NotNull(message = "购买数量不能为空")
        @Min(value = 1, message = "购买数量必须大于0")
        private Integer quantity;
    }
} 