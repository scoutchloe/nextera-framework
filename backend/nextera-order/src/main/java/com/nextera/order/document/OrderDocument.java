package com.nextera.order.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单ElasticSearch文档
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "nextera_order")
public class OrderDocument {

    /**
     * 订单ID
     */
    @Id
    @Field(type = FieldType.Long)
    private Long id;

    /**
     * 订单号
     */
    @Field(type = FieldType.Keyword)
    private String orderNo;

    /**
     * 用户ID
     */
    @Field(type = FieldType.Long)
    private Long userId;

    /**
     * 订单总金额
     */
    @Field(type = FieldType.Double)
    private BigDecimal totalAmount;

    /**
     * 订单状态(1:待支付,2:已支付,3:已发货,4:已完成,5:已取消)
     */
    @Field(type = FieldType.Integer)
    private Integer status;

    /**
     * 支付方式(1:微信,2:支付宝,3:银行卡)
     */
    @Field(type = FieldType.Integer)
    private Integer paymentMethod;

    /**
     * 订单备注
     */
    @Field(type = FieldType.Text, analyzer = "standard")
    private String remark;

    /**
     * 创建时间
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime updatedTime;

    /**
     * 订单明细列表
     */
    @Field(type = FieldType.Nested)
    private List<OrderItemDocument> orderItems;

    /**
     * 数据同步时间
     */
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime syncTime;

    /**
     * 数据来源类型 (INSERT, UPDATE, DELETE)
     */
    @Field(type = FieldType.Keyword)
    private String eventType;

    /**
     * 数据库名
     */
    @Field(type = FieldType.Keyword)
    private String database;

    /**
     * 表名
     */
    @Field(type = FieldType.Keyword)
    private String table;
} 