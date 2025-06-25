package com.nextera.order.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextera.common.exception.BusinessException;
import com.nextera.order.client.IdServiceClientForOrder;
import com.nextera.order.entity.Order;
import com.nextera.order.entity.OrderItem;
import com.nextera.order.mapper.OrderItemMapper;
import com.nextera.order.mapper.OrderMapper;
import com.nextera.order.service.OrderService;
import com.nextera.orderapi.dto.CreateOrderRequest;
import com.nextera.orderapi.dto.OrderQueryRequest;
import com.nextera.orderapi.dto.OrderResponse;
import com.nextera.orderapi.enums.OrderStatus;
import com.nextera.orderapi.enums.PaymentMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private IdServiceClientForOrder idServiceClientForOrder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse createOrder(CreateOrderRequest request) {
        // 1. 生成订单ID和订单号
        Long orderId = idServiceClientForOrder.generateId("order");
        String orderNo = generateOrderNo();

        // 2. 计算订单总金额
        BigDecimal totalAmount = request.getItems().stream()
                .map(item -> item.getProductPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. 创建订单
        Order order = new Order();
        order.setId(orderId);
        order.setOrderNo(orderNo);
        order.setUserId(request.getUserId());
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING_PAYMENT.getCode());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setRemark(request.getRemark());
        order.setCreatedTime(LocalDateTime.now());
        order.setUpdatedTime(LocalDateTime.now());

        int orderResult = orderMapper.insert(order);
        if (orderResult <= 0) {
            throw new BusinessException("创建订单失败");
        }

        // 4. 创建订单明细
        List<OrderItem> orderItems = new ArrayList<>();
        for (CreateOrderRequest.OrderItemDto itemDto : request.getItems()) {
            Long itemId = idServiceClientForOrder.generateId("order");
            OrderItem orderItem = new OrderItem();
            orderItem.setId(itemId);
            orderItem.setOrderId(orderId);
            orderItem.setUserId(request.getUserId()); // 设置用户ID（分片键）
            orderItem.setProductId(itemDto.getProductId());
            orderItem.setProductName(itemDto.getProductName());
            orderItem.setProductPrice(itemDto.getProductPrice());
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setTotalPrice(itemDto.getProductPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity())));
            orderItem.setCreatedTime(LocalDateTime.now());
            orderItems.add(orderItem);
        }

        int itemResult = orderItemMapper.batchInsert(orderItems);
        if (itemResult <= 0) {
            throw new BusinessException("创建订单明细失败");
        }

        log.info("订单创建成功，订单ID：{}，订单号：{}", orderId, orderNo);
        return convertToOrderResponse(order, orderItems);
    }

    @Override
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        List<OrderItem> orderItems = orderItemMapper.selectByOrderId(orderId);
        return convertToOrderResponse(order, orderItems);
    }

    @Override
    public OrderResponse getOrderByOrderNo(String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        List<OrderItem> orderItems = orderItemMapper.selectByOrderId(order.getId());
        return convertToOrderResponse(order, orderItems);
    }

    @Override
    public IPage<OrderResponse> getOrderPage(OrderQueryRequest query) {
        Page<Order> page = new Page<>(query.getCurrent(), query.getSize());
        IPage<Order> orderPage = orderMapper.selectOrderPage(page, query);

        return orderPage.convert(order -> {
            List<OrderItem> orderItems = orderItemMapper.selectByOrderId(order.getId());
            return convertToOrderResponse(order, orderItems);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrderStatus(Long orderId, Integer status) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 验证状态流转是否合法
        if (!isValidStatusTransition(order.getStatus(), status)) {
            throw new BusinessException("订单状态流转不合法");
        }

        int result = orderMapper.updateOrderStatus(orderId, status);
        if (result > 0) {
            log.info("订单状态更新成功，订单ID：{}，新状态：{}", orderId, status);
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 只有待支付和已支付的订单可以取消
        if (!OrderStatus.PENDING_PAYMENT.getCode().equals(order.getStatus()) &&
            !OrderStatus.PAID.getCode().equals(order.getStatus())) {
            throw new BusinessException("该订单状态不允许取消");
        }

        return updateOrderStatus(orderId, OrderStatus.CANCELLED.getCode());
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomStr = String.valueOf((int) (Math.random() * 1000));
        return "ORD" + timestamp + StrUtil.padPre(randomStr, 3, '0');
    }

    /**
     * 验证订单状态流转是否合法
     */
    private boolean isValidStatusTransition(Integer currentStatus, Integer newStatus) {
        return OrderStatus.isValidTransition(currentStatus, newStatus);
    }

    /**
     * 转换为订单响应DTO
     */
    private OrderResponse convertToOrderResponse(Order order, List<OrderItem> orderItems) {
        OrderResponse response = new OrderResponse();
        BeanUtils.copyProperties(order, response);

        // 设置状态描述
        OrderStatus status = OrderStatus.fromCode(order.getStatus());
        if (status != null) {
            response.setStatusDescription(status.getDescription());
        }

        // 设置支付方式描述
        if (order.getPaymentMethod() != null) {
            PaymentMethod paymentMethod = PaymentMethod.fromCode(order.getPaymentMethod());
            if (paymentMethod != null) {
                response.setPaymentMethodDescription(paymentMethod.getDescription());
            }
        }

        // 设置订单明细
        List<OrderResponse.OrderItemResponse> itemResponses = orderItems.stream()
                .map(this::convertToOrderItemResponse)
                .collect(Collectors.toList());
        response.setItems(itemResponses);

        return response;
    }

    /**
     * 转换为订单明细响应DTO
     */
    private OrderResponse.OrderItemResponse convertToOrderItemResponse(OrderItem orderItem) {
        OrderResponse.OrderItemResponse response = new OrderResponse.OrderItemResponse();
        BeanUtils.copyProperties(orderItem, response);
        return response;
    }

    @Override
    public Order getById(Long orderId) {
        return orderMapper.selectById(orderId);
    }

    @Override
    public List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        return orderItemMapper.selectByOrderId(orderId);
    }
} 