package com.nextera.order.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单明细ElasticSearch文档
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDocument {

    /**
     * 明细ID
     */
    @Field(type = FieldType.Long)
    private Long id;

    /**
     * 订单ID
     */
    @Field(type = FieldType.Long)
    private Long orderId;

    /**
     * 用户ID（分片键）
     */
    @Field(type = FieldType.Long)
    private Long userId;

    /**
     * 商品ID
     */
    @Field(type = FieldType.Long)
    private Long productId;

    /**
     * 商品名称
     */
    @Field(type = FieldType.Text, analyzer = "standard")
    private String productName;

    /**
     * 商品单价
     */
    @Field(type = FieldType.Double)
    private BigDecimal productPrice;

    /**
     * 购买数量
     */
    @Field(type = FieldType.Integer)
    private Integer quantity;

    /**
     * 小计金额
     */
    @Field(type = FieldType.Double)
    private BigDecimal totalPrice;

    /**
     * 创建时间
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createdTime;
} 