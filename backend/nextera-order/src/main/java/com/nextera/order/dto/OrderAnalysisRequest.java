package com.nextera.order.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.List;

/**
 * 订单分析请求DTO
 * 
 * @author Nextera Framework
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class OrderAnalysisRequest {

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 用户ID列表（可选）
     */
    private List<Long> userIds;

    /**
     * 订单状态列表（可选）
     */
    private List<Integer> statusList;

    /**
     * 商品ID列表（可选）
     */
    private List<Long> productIds;

    /**
     * 最小金额（可选）
     */
    private Double minAmount;

    /**
     * 最大金额（可选）
     */
    private Double maxAmount;

    /**
     * 分析类型
     */
    private AnalysisType analysisType;

    /**
     * 分组维度
     */
    private GroupDimension groupDimension;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序方向（ASC/DESC）
     */
    private String sortDirection;

    /**
     * 分页大小
     */
    private Integer pageSize;

    /**
     * 页码
     */
    private Integer pageNumber;

    /**
     * 分析类型枚举
     */
    public enum AnalysisType {
        /** 基础统计 */
        BASIC_STATISTICS,
        /** 趋势分析 */
        TREND_ANALYSIS,
        /** 排行榜 */
        RANKING,
        /** 分布分析 */
        DISTRIBUTION,
        /** 对比分析 */
        COMPARISON
    }

    /**
     * 分组维度枚举
     */
    public enum GroupDimension {
        /** 按日期 */
        BY_DATE,
        /** 按用户 */
        BY_USER,
        /** 按商品 */
        BY_PRODUCT,
        /** 按状态 */
        BY_STATUS,
        /** 按金额区间 */
        BY_AMOUNT_RANGE,
        /** 按支付方式 */
        BY_PAYMENT_METHOD
    }
} 