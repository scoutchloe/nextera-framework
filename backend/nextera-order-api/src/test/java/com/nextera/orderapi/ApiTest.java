package com.nextera.orderapi;

import com.nextera.orderapi.dto.CreateOrderRequest;
import com.nextera.orderapi.enums.OrderStatus;
import com.nextera.orderapi.enums.PaymentMethod;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * API测试类
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
public class ApiTest {

    @Test
    public void testOrderStatusEnum() {
        // 测试状态枚举
        OrderStatus pending = OrderStatus.PENDING_PAYMENT;
        assertEquals(Integer.valueOf(1), pending.getCode());
        assertEquals("待支付", pending.getDescription());

        // 测试根据代码获取枚举
        OrderStatus fromCode = OrderStatus.fromCode(2);
        assertEquals(OrderStatus.PAID, fromCode);

        // 测试状态流转验证
        assertTrue(OrderStatus.isValidTransition(1, 2)); // 待支付 -> 已支付
        assertTrue(OrderStatus.isValidTransition(1, 5)); // 待支付 -> 已取消
        assertFalse(OrderStatus.isValidTransition(4, 1)); // 已完成 -> 待支付（不允许）
    }

    @Test
    public void testPaymentMethodEnum() {
        // 测试支付方式枚举
        PaymentMethod wechat = PaymentMethod.WECHAT;
        assertEquals(Integer.valueOf(1), wechat.getCode());
        assertEquals("微信支付", wechat.getDescription());

        // 测试根据代码获取枚举
        PaymentMethod fromCode = PaymentMethod.fromCode(2);
        assertEquals(PaymentMethod.ALIPAY, fromCode);
    }

    @Test
    public void testCreateOrderRequest() {
        // 测试创建订单请求DTO
        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(1001L);
        request.setPaymentMethod(PaymentMethod.WECHAT.getCode());
        request.setRemark("测试订单");

        // 创建订单明细
        List<CreateOrderRequest.OrderItemDto> items = new ArrayList<>();
        CreateOrderRequest.OrderItemDto item = new CreateOrderRequest.OrderItemDto();
        item.setProductId(2001L);
        item.setProductName("测试商品");
        item.setProductPrice(new BigDecimal("99.99"));
        item.setQuantity(2);
        items.add(item);

        request.setItems(items);

        // 验证数据
        assertEquals(Long.valueOf(1001L), request.getUserId());
        assertEquals(Integer.valueOf(1), request.getPaymentMethod());
        assertEquals("测试订单", request.getRemark());
        assertEquals(1, request.getItems().size());
        assertEquals("测试商品", request.getItems().get(0).getProductName());
    }
} 