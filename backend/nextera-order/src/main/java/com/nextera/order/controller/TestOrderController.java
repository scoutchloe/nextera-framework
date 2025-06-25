package com.nextera.order.controller;

import com.nextera.common.core.Result;
import com.nextera.order.client.IdServiceClientForOrder;
import com.nextera.order.service.OrderService;
import com.nextera.orderapi.dto.CreateOrderRequest;
import com.nextera.orderapi.dto.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 订单测试控制器
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/test/orders")
@Tag(name = "订单测试", description = "订单模块测试接口")
public class TestOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private IdServiceClientForOrder idServiceClientForOrder;

    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查订单服务是否正常")
    public Result<String> health() {
        return Result.success("Order service is running!");
    }

    @GetMapping("/test-id-generator")
    @Operation(summary = "测试ID生成器", description = "测试分布式ID生成")
    public Result<Map<String, Object>> testIdGenerator() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 测试生成订单ID
            Long orderId = idServiceClientForOrder.generateId("order");
            result.put("orderId", orderId);
            
            // 测试生成订单明细ID
            Long orderItemId = idServiceClientForOrder.generateId("order");
            result.put("orderItemId", orderItemId);
            
            result.put("status", "success");
            log.info("ID生成测试成功，订单ID：{}，明细ID：{}", orderId, orderItemId);
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
            log.error("ID生成测试失败", e);
        }
        
        return Result.success(result);
    }

    @PostMapping("/test-create-order")
    @Operation(summary = "测试创建订单", description = "创建测试订单数据")
    public Result<OrderResponse> testCreateOrder(@RequestParam(defaultValue = "1001") Long userId) {
        try {
            // 构造测试订单数据
            CreateOrderRequest request = new CreateOrderRequest();
            request.setUserId(userId);
            request.setPaymentMethod(1); // 微信支付
            request.setRemark("测试订单");

            // 添加订单明细
            CreateOrderRequest.OrderItemDto item1 = new CreateOrderRequest.OrderItemDto();
            item1.setProductId(2001L);
            item1.setProductName("测试商品1");
            item1.setProductPrice(new BigDecimal("99.99"));
            item1.setQuantity(2);

            CreateOrderRequest.OrderItemDto item2 = new CreateOrderRequest.OrderItemDto();
            item2.setProductId(2002L);
            item2.setProductName("测试商品2");
            item2.setProductPrice(new BigDecimal("158.80"));
            item2.setQuantity(1);

            request.setItems(Arrays.asList(item1, item2));

            // 创建订单
            OrderResponse response = orderService.createOrder(request);
            log.info("测试订单创建成功，订单ID：{}，用户ID：{}", response.getId(), userId);
            
            return Result.success(response);
        } catch (Exception e) {
            log.error("测试订单创建失败", e);
            return Result.error("创建测试订单失败：" + e.getMessage());
        }
    }

    @GetMapping("/test-sharding")
    @Operation(summary = "测试分库分表", description = "测试不同用户ID的分库分表效果")
    public Result<Map<String, Object>> testSharding() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 测试多个用户ID的订单创建，验证分库分表
            Long[] userIds = {1001L, 1002L, 1003L, 1004L, 1005L};
            
            for (Long userId : userIds) {
                CreateOrderRequest request = new CreateOrderRequest();
                request.setUserId(userId);
                request.setPaymentMethod(1);
                request.setRemark("分库分表测试订单-用户" + userId);

                CreateOrderRequest.OrderItemDto item = new CreateOrderRequest.OrderItemDto();
                item.setProductId(3001L);
                item.setProductName("测试商品");
                item.setProductPrice(new BigDecimal("50.00"));
                item.setQuantity(1);
                request.setItems(Arrays.asList(item));

                OrderResponse response = orderService.createOrder(request);
                result.put("user_" + userId + "_order", response.getId());
                
                // 计算分片信息
                int dbIndex = Math.abs(userId.hashCode()) % 4;
                int tableIndex = Math.abs(userId.hashCode()) % 4;
                result.put("user_" + userId + "_shard_info", 
                          "db_" + dbIndex + ".t_order_" + tableIndex);
            }
            
            result.put("status", "success");
            log.info("分库分表测试完成");
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", e.getMessage());
            log.error("分库分表测试失败", e);
        }
        
        return Result.success(result);
    }
} 