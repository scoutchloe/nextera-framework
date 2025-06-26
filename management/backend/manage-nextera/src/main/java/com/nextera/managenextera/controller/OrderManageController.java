package com.nextera.managenextera.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nextera.managenextera.annotation.OperationLog;
import com.nextera.managenextera.common.Result;
import com.nextera.managenextera.dto.OrderManageDTO;
import com.nextera.managenextera.dto.OrderSearchDTO;
import com.nextera.managenextera.service.ExcelExportService;
import com.nextera.managenextera.service.OrderManageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * 订单管理控制器
 *
 * @author nextera
 * @since 2025-01-01
 */
@Tag(name = "订单管理", description = "管理端订单管理相关接口")
@RestController
@RequestMapping("/order")
@Slf4j
public class OrderManageController {

    @Autowired
    private OrderManageService orderManageService;

    @Autowired
    private ExcelExportService excelExportService;

    @Operation(summary = "分页查询订单", description = "分页查询订单列表（基于ES）")
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('order:list:view')")
    @OperationLog(module = "订单管理", type = OperationLog.OperationType.QUERY, description = "分页查询订单")
    public Result<IPage<OrderManageDTO>> getOrderPage(@Validated @RequestBody OrderSearchDTO searchDTO) {
        log.info("分页查询订单，搜索条件: {}", searchDTO);

        IPage<OrderManageDTO> result = orderManageService.getOrderPage(searchDTO);
        return Result.success(result);
    }

    @Operation(summary = "根据ID获取订单详情", description = "根据订单ID获取订单详细信息")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('order:list:view')")
    @OperationLog(module = "订单管理", type = OperationLog.OperationType.QUERY, description = "查看订单详情")
    public Result<OrderManageDTO> getOrderDetail(
            @Parameter(description = "订单ID") @PathVariable Long id) {

        log.info("根据ID查询订单详情: {}", id);

        OrderManageDTO order = orderManageService.getOrderDetail(id);
        if (order != null) {
            return Result.success(order);
        } else {
            return Result.error("订单不存在");
        }
    }

    @Operation(summary = "根据订单号获取订单详情", description = "根据订单号获取订单详细信息")
    @GetMapping("/orderNo/{orderNo}")
    @PreAuthorize("hasAuthority('order:list:view')")
    @OperationLog(module = "订单管理", type = OperationLog.OperationType.QUERY, description = "根据订单号查看订单详情")
    public Result<OrderManageDTO> getOrderDetailByOrderNo(
            @Parameter(description = "订单号") @PathVariable String orderNo) {

        log.info("根据订单号查询订单详情: {}", orderNo);

        OrderManageDTO order = orderManageService.getOrderDetailByOrderNo(orderNo);
        if (order != null) {
            return Result.success(order);
        } else {
            return Result.error("订单不存在");
        }
    }

    @Operation(summary = "更新订单状态", description = "更新订单状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('order:edit')")
    @OperationLog(module = "订单管理", type = OperationLog.OperationType.UPDATE, description = "更新订单状态")
    public Result<Boolean> updateOrderStatus(
            @Parameter(description = "订单ID") @PathVariable Long id,
            @Parameter(description = "新状态") @RequestParam Integer status) {

        log.info("更新订单状态: orderId={}, status={}", id, status);

        boolean success = orderManageService.updateOrderStatus(id, status);
        if (success) {
            return Result.success("订单状态更新成功", true);
        } else {
            return Result.error("订单状态更新失败");
        }
    }

    @Operation(summary = "批量更新订单状态", description = "批量更新订单状态")
    @PutMapping("/batch/status")
    @PreAuthorize("hasAuthority('order:edit')")
    @OperationLog(module = "订单管理", type = OperationLog.OperationType.UPDATE, description = "批量更新订单状态")
    public Result<Boolean> batchUpdateOrderStatus(
            @Parameter(description = "订单ID列表") @RequestParam List<Long> orderIds,
            @Parameter(description = "新状态") @RequestParam Integer status) {

        log.info("批量更新订单状态: orderIds={}, status={}", orderIds, status);

        boolean success = orderManageService.batchUpdateOrderStatus(orderIds, status);
        return Result.success("批量更新订单状态成功", success);

    }

    @Operation(summary = "高级搜索订单", description = "高级搜索订单")
    @PostMapping("/search")
    @PreAuthorize("hasAuthority('order:list:view')")
    @OperationLog(module = "订单管理", type = OperationLog.OperationType.QUERY, description = "高级搜索订单")
    public Result<List<OrderManageDTO>> advancedSearch(@Validated @RequestBody OrderSearchDTO searchDTO) {
        log.info("高级搜索订单: {}", searchDTO);

        List<OrderManageDTO> result = orderManageService.advancedSearch(searchDTO);
        return Result.success(result);
    }

    @Operation(summary = "导出订单数据", description = "导出订单数据到Excel")
    @PostMapping("/export")
    @PreAuthorize("hasAuthority('order:export')")
    @OperationLog(module = "订单管理", type = OperationLog.OperationType.EXPORT, description = "导出订单数据")
    public void exportOrders(@Validated @RequestBody OrderSearchDTO searchDTO, 
                           HttpServletResponse response) throws IOException {
        log.info("导出订单数据: {}", searchDTO);
        
        excelExportService.exportOrders(searchDTO, response);
    }

    @Operation(summary = "获取订单状态分布", description = "获取订单状态分布统计")
    @GetMapping("/status/distribution")
    @PreAuthorize("hasAuthority('order:list:view')")
    @OperationLog(module = "订单管理", type = OperationLog.OperationType.QUERY, description = "查看订单状态分布")
    public Result<List<OrderManageDTO>> getOrderStatusDistribution() {
        log.info("获取订单状态分布");

        List<OrderManageDTO> result = orderManageService.getOrderStatusDistribution();
        return Result.success(result);
    }
} 