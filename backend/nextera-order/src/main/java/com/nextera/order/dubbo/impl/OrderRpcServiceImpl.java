package com.nextera.order.dubbo.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nextera.common.core.Result;
import com.nextera.order.service.OrderService;
import com.nextera.orderapi.dto.CreateOrderRequest;
import com.nextera.orderapi.dto.OrderQueryRequest;
import com.nextera.orderapi.dto.OrderResponse;
import com.nextera.orderapi.service.OrderRpcService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 订单RPC服务实现类
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
@Slf4j
@DubboService(version = "1.0.0")
@Service
public class OrderRpcServiceImpl implements OrderRpcService {

    @Autowired
    private OrderService orderService;

    @Override
    public Result<OrderResponse> createOrder(CreateOrderRequest request) {
        try {
            log.info("RPC创建订单请求：{}", request);
            OrderResponse response = orderService.createOrder(request);
            return Result.success(response);
        } catch (Exception e) {
            log.error("RPC创建订单失败", e);
            return Result.error(e.getMessage());
        }
    }

    @Override
    public Result<OrderResponse> getOrderById(Long orderId) {
        try {
            log.info("RPC查询订单，ID：{}", orderId);
            OrderResponse response = orderService.getOrderById(orderId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("RPC查询订单失败", e);
            return Result.error(e.getMessage());
        }
    }

    @Override
    public Result<OrderResponse> getOrderByOrderNo(String orderNo) {
        try {
            log.info("Dubbo RPC: 根据订单号查询订单, orderNo: {}", orderNo);
            OrderResponse response = orderService.getOrderByOrderNo(orderNo);
            return Result.success(response);
        } catch (Exception e) {
            log.error("Dubbo RPC: 根据订单号查询订单失败", e);
            return Result.error(e.getMessage());
        }
    }

    @Override
    public Result<IPage<OrderResponse>> getUserOrders(Long userId, Integer current, Integer size) {
        try {
            log.info("RPC查询用户订单，用户ID：{}", userId);
            
            OrderQueryRequest query = new OrderQueryRequest();
            query.setUserId(userId);
            query.setCurrent(current);
            query.setSize(size);
            
            IPage<OrderResponse> page = orderService.getOrderPage(query);
            return Result.success(page);
        } catch (Exception e) {
            log.error("RPC查询用户订单失败", e);
            return Result.error(e.getMessage());
        }
    }

    @Override
    public Result<Boolean> updateOrderStatus(Long orderId, Integer status) {
        try {
            log.info("RPC更新订单状态，订单ID：{}，新状态：{}", orderId, status);
            boolean result = orderService.updateOrderStatus(orderId, status);
            return Result.success(result);
        } catch (Exception e) {
            log.error("RPC更新订单状态失败", e);
            return Result.error(e.getMessage());
        }
    }

    @Override
    public Result<Boolean> cancelOrder(Long orderId) {
        try {
            log.info("Dubbo RPC: 取消订单, orderId: {}", orderId);
            Boolean result = orderService.cancelOrder(orderId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("Dubbo RPC: 取消订单失败", e);
            return Result.error(e.getMessage());
        }
    }
} 