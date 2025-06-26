package com.nextera.managenextera.controller;

import com.nextera.managenextera.annotation.OperationLog;
import com.nextera.managenextera.common.Result;
import com.nextera.managenextera.dto.OrderStatisticsDTO;
import com.nextera.managenextera.service.OrderStatisticsService;
import com.nextera.managenextera.service.ExcelExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * 订单统计控制器
 *
 * @author nextera
 * @since 2025-01-01
 */
@Tag(name = "订单统计", description = "订单统计分析相关接口")
@RestController
@RequestMapping("/statistics")
@Slf4j
public class OrderStatisticsController {

    @Autowired
    @Qualifier("orderStatisticsESService")
    private OrderStatisticsService orderStatisticsService;

    @Autowired
    private ExcelExportService excelExportService;

    @Operation(summary = "获取概览统计", description = "获取Dashboard概览统计数据")
    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('dashboard:view')")
    @OperationLog(module = "订单统计", type = OperationLog.OperationType.QUERY, description = "查看概览统计")
    public Result<OrderStatisticsDTO.OverviewStatistics> getOverviewStatistics() {
        log.info("获取概览统计");

        try {
            OrderStatisticsDTO.OverviewStatistics result = orderStatisticsService.getOverviewStatistics();
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取概览统计失败", e);
            return Result.error("获取概览统计失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取实时统计", description = "获取实时统计数据")
    @GetMapping("/realtime")
    @PreAuthorize("hasAuthority('dashboard:view')")
    @OperationLog(module = "订单统计", type = OperationLog.OperationType.QUERY, description = "查看实时统计")
    public Result<OrderStatisticsDTO.RealtimeStatistics> getRealtimeStatistics() {
        log.info("获取实时统计");

        try {
            OrderStatisticsDTO.RealtimeStatistics result = orderStatisticsService.getRealtimeStatistics();
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取实时统计失败", e);
            return Result.error("获取实时统计失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取订单趋势", description = "获取订单趋势分析数据")
    @GetMapping("/trend")
    @PreAuthorize("hasAuthority('order:statistics')")
    @OperationLog(module = "订单统计", type = OperationLog.OperationType.QUERY, description = "查看订单趋势")
    public Result<List<OrderStatisticsDTO.TrendStatistics>> getOrderTrend(
            @Parameter(description = "开始日期") @RequestParam String startDate,
            @Parameter(description = "结束日期") @RequestParam String endDate) {

        log.info("获取订单趋势: {} - {}", startDate, endDate);

        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            // 验证日期范围
            if (start.isAfter(end)) {
                return Result.error("开始日期不能大于结束日期");
            }

            List<OrderStatisticsDTO.TrendStatistics> result = orderStatisticsService.getOrderTrend(start, end);
            return Result.success(result);

        } catch (DateTimeParseException e) {
            log.error("日期格式错误: startDate={}, endDate={}", startDate, endDate, e);
            return Result.error("日期格式错误，请使用 yyyy-MM-dd 格式");
        } catch (Exception e) {
            log.error("获取订单趋势失败", e);
            return Result.error("获取订单趋势失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取订单状态分布", description = "获取订单状态分布统计")
    @GetMapping("/status")
    @PreAuthorize("hasAuthority('order:statistics')")
    @OperationLog(module = "订单统计", type = OperationLog.OperationType.QUERY, description = "查看订单状态分布")
    public Result<List<OrderStatisticsDTO.StatusDistribution>> getOrderStatusDistribution(
            @Parameter(description = "开始日期") @RequestParam String startDate,
            @Parameter(description = "结束日期") @RequestParam String endDate) {

        log.info("获取订单状态分布: {} - {}", startDate, endDate);

        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            if (start.isAfter(end)) {
                return Result.error("开始日期不能大于结束日期");
            }

            List<OrderStatisticsDTO.StatusDistribution> result = orderStatisticsService.getOrderStatusDistribution(start, end);
            return Result.success(result);

        } catch (DateTimeParseException e) {
            log.error("日期格式错误: startDate={}, endDate={}", startDate, endDate, e);
            return Result.error("日期格式错误，请使用 yyyy-MM-dd 格式");
        } catch (Exception e) {
            log.error("获取订单状态分布失败", e);
            return Result.error("获取订单状态分布失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取商品销售排行榜", description = "获取商品销售排行榜")
    @GetMapping("/products")
    @PreAuthorize("hasAuthority('order:statistics')")
    @OperationLog(module = "订单统计", type = OperationLog.OperationType.QUERY, description = "查看商品销售排行榜")
    public Result<List<OrderStatisticsDTO.ProductSalesRanking>> getProductSalesRanking(
            @Parameter(description = "开始日期") @RequestParam String startDate,
            @Parameter(description = "结束日期") @RequestParam String endDate,
            @Parameter(description = "返回条数") @RequestParam(defaultValue = "10") Integer limit) {

        log.info("获取商品销售排行榜: {} - {}, limit: {}", startDate, endDate, limit);

        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            if (start.isAfter(end)) {
                return Result.error("开始日期不能大于结束日期");
            }

            if (limit <= 0 || limit > 100) {
                return Result.error("返回条数必须在1-100之间");
            }

            List<OrderStatisticsDTO.ProductSalesRanking> result = orderStatisticsService.getProductSalesRanking(start, end, limit);
            return Result.success(result);

        } catch (DateTimeParseException e) {
            log.error("日期格式错误: startDate={}, endDate={}", startDate, endDate, e);
            return Result.error("日期格式错误，请使用 yyyy-MM-dd 格式");
        } catch (Exception e) {
            log.error("获取商品销售排行榜失败", e);
            return Result.error("获取商品销售排行榜失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取用户订单分析", description = "获取用户订单分析数据")
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('order:statistics')")
    @OperationLog(module = "订单统计", type = OperationLog.OperationType.QUERY, description = "查看用户订单分析")
    public Result<List<OrderStatisticsDTO.UserOrderAnalysis>> getUserOrderAnalysis(
            @Parameter(description = "开始日期") @RequestParam String startDate,
            @Parameter(description = "结束日期") @RequestParam String endDate,
            @Parameter(description = "返回条数") @RequestParam(defaultValue = "10") Integer limit) {

        log.info("获取用户订单分析: {} - {}, limit: {}", startDate, endDate, limit);

        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            if (start.isAfter(end)) {
                return Result.error("开始日期不能大于结束日期");
            }

            if (limit <= 0 || limit > 100) {
                return Result.error("返回条数必须在1-100之间");
            }

            List<OrderStatisticsDTO.UserOrderAnalysis> result = orderStatisticsService.getUserOrderAnalysis(start, end, limit);
            return Result.success(result);

        } catch (DateTimeParseException e) {
            log.error("日期格式错误: startDate={}, endDate={}", startDate, endDate, e);
            return Result.error("日期格式错误，请使用 yyyy-MM-dd 格式");
        } catch (Exception e) {
            log.error("获取用户订单分析失败", e);
            return Result.error("获取用户订单分析失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取订单金额分布", description = "获取订单金额分布统计")
    @GetMapping("/amount")
    @PreAuthorize("hasAuthority('order:statistics')")
    @OperationLog(module = "订单统计", type = OperationLog.OperationType.QUERY, description = "查看订单金额分布")
    public Result<Map<String, Long>> getOrderAmountDistribution(
            @Parameter(description = "开始日期") @RequestParam String startDate,
            @Parameter(description = "结束日期") @RequestParam String endDate) {

        log.info("获取订单金额分布: {} - {}", startDate, endDate);

        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            if (start.isAfter(end)) {
                return Result.error("开始日期不能大于结束日期");
            }

            Map<String, Long> result = orderStatisticsService.getOrderAmountDistribution(start, end);
            return Result.success(result);

        } catch (DateTimeParseException e) {
            log.error("日期格式错误: startDate={}, endDate={}", startDate, endDate, e);
            return Result.error("日期格式错误，请使用 yyyy-MM-dd 格式");
        } catch (Exception e) {
            log.error("获取订单金额分布失败", e);
            return Result.error("获取订单金额分布失败: " + e.getMessage());
        }
    }

    @Operation(summary = "刷新统计缓存", description = "刷新所有统计缓存")
    @PostMapping("/cache/refresh")
    @PreAuthorize("hasAuthority('order:cache')")
    @OperationLog(module = "订单统计", type = OperationLog.OperationType.CACHE, description = "刷新统计缓存")
    public Result<Boolean> refreshStatisticsCache() {
        log.info("刷新统计缓存");

        boolean success = orderStatisticsService.refreshStatisticsCache();

        return Result.success("统计缓存刷新成功", success);

    }
} 