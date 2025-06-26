package com.nextera.managenextera.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单ES实体类
 *
 * @author nextera
 * @since 2025-01-01
 */
@Data
@Document(indexName = "nextera_order")
public class OrderES {

    @Field(type = FieldType.Keyword)
    private String _class;

    @Field(type = FieldType.Long)
    private Long id;

    @Field(type = FieldType.Keyword)
    private String orderNo;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Keyword)
    private String userName;

    @Field(type = FieldType.Keyword)
    private String userPhone;

    @Field(type = FieldType.Keyword)
    private String userEmail;

    @Field(type = FieldType.Integer)
    private Integer status;

    @Field(type = FieldType.Keyword)
    private String statusName;

    @Field(type = FieldType.Double)
    private BigDecimal totalAmount;

    @Field(type = FieldType.Double)
    private BigDecimal payAmount;

    @Field(type = FieldType.Double)
    private BigDecimal discountAmount;

    @Field(type = FieldType.Integer)
    private Integer paymentMethod;

    @Field(type = FieldType.Integer)
    private Integer payStatus;

    @Field(type = FieldType.Keyword)
    private String payStatusName;

    @Field(type = FieldType.Keyword)
    private String payTransactionId;

    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime payTime;

    @Field(type = FieldType.Integer)
    private Integer deliveryStatus;

    @Field(type = FieldType.Keyword)
    private String deliveryStatusName;

    @Field(type = FieldType.Keyword)
    private String deliveryCompany;

    @Field(type = FieldType.Keyword)
    private String deliveryNo;

    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliveryTime;

    @Field(type = FieldType.Text)
    private String receiverName;

    @Field(type = FieldType.Keyword)
    private String receiverPhone;

    @Field(type = FieldType.Text)
    private String receiverAddress;

    @Field(type = FieldType.Text)
    private String remark;

    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd'T'HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss.SSS")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdTime;

    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd'T'HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss.SSS")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedTime;

    @Field(type = FieldType.Nested)
    private List<OrderItemES> orderItems;

    // Canal同步相关字段
    @Field(type = FieldType.Date, pattern = "yyyy-MM-dd'T'HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss.SSS")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime syncTime;

    @Field(type = FieldType.Keyword)
    private String eventType;

    @Field(type = FieldType.Keyword)
    private String database;

    @Field(type = FieldType.Keyword)
    private String table;

    /**
     * 订单明细ES实体类
     */
    @Data
    public static class OrderItemES {
        @Field(type = FieldType.Long)
        private Long id;

        @Field(type = FieldType.Long)
        private Long orderId;

        @Field(type = FieldType.Long)
        private Long productId;

        @Field(type = FieldType.Keyword)
        private String productName;

        @Field(type = FieldType.Keyword)
        private String productSku;

        @Field(type = FieldType.Text)
        private String productImage;

        @Field(type = FieldType.Double)
        private BigDecimal productPrice;

        @Field(type = FieldType.Integer)
        private Integer quantity;

        @Field(type = FieldType.Double)
        private BigDecimal totalPrice;

        @Field(type = FieldType.Text)
        private String specifications;

        @Field(type = FieldType.Date, pattern = "yyyy-MM-dd'T'HH:mm:ss||yyyy-MM-dd'T'HH:mm:ss.SSS")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdTime;
    }
} 