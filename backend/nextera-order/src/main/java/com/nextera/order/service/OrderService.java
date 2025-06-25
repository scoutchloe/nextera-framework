package com.nextera.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nextera.orderapi.dto.CreateOrderRequest;
import com.nextera.orderapi.dto.OrderQueryRequest;
import com.nextera.orderapi.dto.OrderResponse;

/**
 * 订单服务接口
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
public interface OrderService {

    /**
     * 创建订单
     *
     * @param request 创建订单请求
     * @return 订单响应
     */
    OrderResponse createOrder(CreateOrderRequest request);

    /**
     * 根据订单ID查询订单
     *
     * @param orderId 订单ID
     * @return 订单响应
     */
    OrderResponse getOrderById(Long orderId);

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单响应
     */
    OrderResponse getOrderByOrderNo(String orderNo);

    /**
     * 分页查询订单
     *
     * @param query 查询条件
     * @return 订单分页结果
     */
    IPage<OrderResponse> getOrderPage(OrderQueryRequest query);

    /**
     * 更新订单状态
     *
     * @param orderId 订单ID
     * @param status 新状态
     * @return 是否成功
     */
    boolean updateOrderStatus(Long orderId, Integer status);

    /**
     * 取消订单
     *
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean cancelOrder(Long orderId);
} 