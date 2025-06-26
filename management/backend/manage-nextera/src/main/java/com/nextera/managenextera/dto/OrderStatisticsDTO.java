package com.nextera.managenextera.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单统计数据传输对象
 *
 * @author nextera
 * @since 2025-01-01
 */
@Data
@Schema(description = "订单统计数据")
public class OrderStatisticsDTO {

    /**
     * 概览统计
     */
    @Data
    @Schema(description = "概览统计")
    public static class OverviewStatistics {

        @Schema(description = "今日订单数")
        private Long todayOrderCount;

        @Schema(description = "今日订单金额")
        private BigDecimal todayOrderAmount;

        @Schema(description = "本月订单数")
        private Long monthOrderCount;

        @Schema(description = "本月订单金额")
        private BigDecimal monthOrderAmount;

        @Schema(description = "总订单数")
        private Long totalOrderCount;

        @Schema(description = "总订单金额")
        private BigDecimal totalOrderAmount;

        @Schema(description = "待支付订单数")
        private Long pendingOrderCount;

        @Schema(description = "已支付订单数")
        private Long paidOrderCount;

        @Schema(description = "已完成订单数")
        private Long completedOrderCount;

        @Schema(description = "已取消订单数")
        private Long cancelledOrderCount;

        @Schema(description = "统计时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime statisticsTime;
    }

    /**
     * 趋势统计
     */
    @Data
    @Schema(description = "趋势统计")
    public static class TrendStatistics {

        @Schema(description = "日期")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;

        @Schema(description = "订单数量")
        private Long orderCount;

        @Schema(description = "订单金额")
        private BigDecimal orderAmount;

        @Schema(description = "新用户数")
        private Long newUserCount;
    }

    /**
     * 状态分布统计
     */
    @Data
    @Schema(description = "状态分布统计")
    public static class StatusDistribution {

        @Schema(description = "状态")
        private Integer status;

        @Schema(description = "状态描述")
        private String statusDescription;

        @Schema(description = "数量")
        private Long count;

        @Schema(description = "占比")
        private Double percentage;
    }

    /**
     * 商品销售排行
     */
    @Data
    @Schema(description = "商品销售排行")
    public static class ProductSalesRanking {

        @Schema(description = "商品ID")
        private Long productId;

        @Schema(description = "商品名称")
        private String productName;

        @Schema(description = "销售数量")
        private Long salesCount;

        @Schema(description = "销售金额")
        private BigDecimal salesAmount;

        @Schema(description = "排名")
        private Integer rank;
    }

    /**
     * 用户订单分析
     */
    @Data
    @Schema(description = "用户订单分析")
    public static class UserOrderAnalysis {

        @Schema(description = "用户ID")
        private Long userId;

        @Schema(description = "用户名")
        private String username;

        @Schema(description = "订单数量")
        private Long orderCount;

        @Schema(description = "订单总金额")
        private BigDecimal totalAmount;

        @Schema(description = "平均订单金额")
        private BigDecimal avgOrderAmount;

        @Schema(description = "最后下单时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastOrderTime;
    }

    /**
     * 实时统计
     */
    @Data
    @Schema(description = "实时统计")
    public static class RealtimeStatistics {

        @Schema(description = "过去1小时订单数")
        private Long lastHourOrderCount;

        @Schema(description = "过去24小时订单数")
        private Long last24HourOrderCount;

        @Schema(description = "过去7天订单数")
        private Long last7DayOrderCount;

        @Schema(description = "过去30天订单数")
        private Long last30DayOrderCount;

        @Schema(description = "过去1小时订单金额")
        private BigDecimal lastHourOrderAmount;

        @Schema(description = "过去24小时订单金额")
        private BigDecimal last24HourOrderAmount;

        @Schema(description = "过去7天订单金额")
        private BigDecimal last7DayOrderAmount;

        @Schema(description = "过去30天订单金额")
        private BigDecimal last30DayOrderAmount;

        @Schema(description = "统计时间")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime statisticsTime;
    }
} 