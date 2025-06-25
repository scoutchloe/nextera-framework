package com.nextera.orderapi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nextera.common.core.Result;
import com.nextera.orderapi.dto.CreateOrderRequest;
import com.nextera.orderapi.dto.OrderResponse;

/**
 * 订单RPC服务接口
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
public interface OrderRpcService {

    /**
     * 创建订单
     *
     * @param request 创建订单请求
     * @return 订单响应
     */
    Result<OrderResponse> createOrder(CreateOrderRequest request);

    /**
     * 根据订单ID查询订单
     *
     * @param orderId 订单ID
     * @return 订单响应
     */
    Result<OrderResponse> getOrderById(Long orderId);

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单响应
     */
    Result<OrderResponse> getOrderByOrderNo(String orderNo);

    /**
     * 查询用户订单
     *
     * @param userId 用户ID
     * @param current 当前页
     * @param size 页面大小
     * @return 订单分页结果
     */
    Result<IPage<OrderResponse>> getUserOrders(Long userId, Integer current, Integer size);

    /**
     * 更新订单状态
     *
     * @param orderId 订单ID
     * @param status 新状态
     * @return 是否成功
     */
    Result<Boolean> updateOrderStatus(Long orderId, Integer status);

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @return 是否成功
     */
    Result<Boolean> cancelOrder(Long orderId);
} 