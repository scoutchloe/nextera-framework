package com.nextera.managenextera.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单高级搜索参数对象
 *
 * @author nextera
 * @since 2025-01-01
 */
@Data
@Schema(description = "订单搜索条件")
public class OrderSearchDTO {

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "订单状态列表")
    private List<Integer> statusList;

    @Schema(description = "支付方式列表")
    private List<Integer> paymentMethodList;

    @Schema(description = "最小金额")
    private BigDecimal minAmount;

    @Schema(description = "最大金额")
    private BigDecimal maxAmount;

    @Schema(description = "创建时间开始")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTimeStart;

    @Schema(description = "创建时间结束")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTimeEnd;

    @Schema(description = "更新时间开始")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTimeStart;

    @Schema(description = "更新时间结束")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTimeEnd;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "页码")
    private Integer current = 1;

    @Schema(description = "每页大小")
    private Integer size = 10;

    @Schema(description = "排序字段")
    private String sortField = "createdTime";

    @Schema(description = "排序方向")
    private String sortOrder = "desc";
} 