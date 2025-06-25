package com.nextera.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nextera.common.core.Result;
import com.nextera.orderapi.dto.CreateOrderRequest;
import com.nextera.orderapi.dto.OrderQueryRequest;
import com.nextera.orderapi.dto.OrderResponse;
import com.nextera.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * 订单控制器
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/orders")
@Tag(name = "订单管理", description = "订单相关接口")
@Validated
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    @Operation(summary = "创建订单", description = "创建新的订单")
    public Result<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        log.info("创建订单请求：{}", request);
        OrderResponse response = orderService.createOrder(request);
        return Result.success(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询订单", description = "根据订单ID查询订单详情")
    public Result<OrderResponse> getOrderById(
            @Parameter(description = "订单ID", required = true)
            @PathVariable("id") @NotNull Long id) {
        log.info("查询订单，ID：{}", id);
        OrderResponse response = orderService.getOrderById(id);
        return Result.success(response);
    }

    @GetMapping("/orderNo/{orderNo}")
    @Operation(summary = "根据订单号查询订单", description = "根据订单号查询订单详情")
    public Result<OrderResponse> getOrderByOrderNo(
            @Parameter(description = "订单号", required = true)
            @PathVariable("orderNo") String orderNo) {
        log.info("查询订单，订单号：{}", orderNo);
        OrderResponse response = orderService.getOrderByOrderNo(orderNo);
        return Result.success(response);
    }

    @GetMapping
    @Operation(summary = "分页查询订单", description = "分页查询订单列表")
    public Result<IPage<OrderResponse>> getOrderPage(@Valid OrderQueryRequest query) {
        log.info("分页查询订单，条件：{}", query);
        IPage<OrderResponse> page = orderService.getOrderPage(query);
        return Result.success(page);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "查询用户订单", description = "查询指定用户的订单列表")
    public Result<IPage<OrderResponse>> getUserOrders(
            @Parameter(description = "用户ID", required = true)
            @PathVariable("userId") @NotNull Long userId,
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        log.info("查询用户订单，用户ID：{}", userId);
        
        OrderQueryRequest query = new OrderQueryRequest();
        query.setUserId(userId);
        query.setCurrent(current);
        query.setSize(size);
        
        IPage<OrderResponse> page = orderService.getOrderPage(query);
        return Result.success(page);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新订单状态", description = "更新指定订单的状态")
    public Result<Boolean> updateOrderStatus(
            @Parameter(description = "订单ID", required = true)
            @PathVariable("id") @NotNull Long id,
            @Parameter(description = "新状态", required = true)
            @RequestParam("status") @NotNull Integer status) {
        log.info("更新订单状态，订单ID：{}，新状态：{}", id, status);
        boolean result = orderService.updateOrderStatus(id, status);
        return Result.success(result);
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消订单", description = "取消指定的订单")
    public Result<Boolean> cancelOrder(
            @Parameter(description = "订单ID", required = true)
            @PathVariable("id") @NotNull Long id) {
        log.info("取消订单，订单ID：{}", id);
        boolean result = orderService.cancelOrder(id);
        return Result.success(result);
    }
} 