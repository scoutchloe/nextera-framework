package com.nextera.order.controller;

import com.nextera.common.core.Result;
import com.nextera.order.service.OrderService;
import com.nextera.orderapi.dto.CreateOrderRequest;
import com.nextera.orderapi.dto.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 分库分表测试控制器
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/sharding-test")
@Tag(name = "分库分表测试", description = "用于测试分库分表功能")
public class ShardingTestController {

    @Autowired
    private OrderService orderService;

    @Operation(summary = "测试分库分表", description = "创建多个订单验证分库分表效果")
    @PostMapping("/test-sharding")
    public Result<List<OrderResponse>> testSharding(@RequestParam(defaultValue = "10", name = "count") int count) {
        log.info("开始分库分表测试，创建 {} 个订单", count);
        
        List<OrderResponse> responses = new ArrayList<>();
        
        try {
            for (int i = 0; i < count; i++) {
                // 创建不同用户ID的订单，触发不同的分片
                Long userId = (long) (i + 1000);
                
                CreateOrderRequest request = new CreateOrderRequest();
                request.setUserId(userId);
                request.setPaymentMethod(1); // 微信支付
                request.setRemark("分库分表测试订单 - " + i);
                
                // 创建订单明细 - 暂时为空，等API模块完善后再添加
                 CreateOrderRequest.OrderItemDto item = new CreateOrderRequest.OrderItemDto();
                 item.setProductId((long) (i + 1));
                 item.setProductName("测试商品" + i);
                 item.setProductPrice(new BigDecimal("99.99"));
                 item.setQuantity(1);
                
                 List<CreateOrderRequest.OrderItemDto> items = new ArrayList<>();
                 items.add(item);
                 request.setItems(items);
                
                // 创建订单
                OrderResponse response = orderService.createOrder(request);
                responses.add(response);
                
                log.info("创建订单成功 - 用户ID: {}, 订单ID: {}, 订单号: {}", 
                    userId, response.getId(), response.getOrderNo());
            }
            
            log.info("分库分表测试完成，共创建 {} 个订单", responses.size());
            return Result.success(responses);
            
        } catch (Exception e) {
            log.error("分库分表测试失败", e);
            return Result.error("测试失败: " + e.getMessage());
        }
    }

    @Operation(summary = "查询分片统计", description = "统计各个分片中的数据分布")
    @GetMapping("/sharding-stats")
    public Result<String> getShardingStats() {
        try {
            StringBuilder stats = new StringBuilder();
            stats.append("分库分表统计信息：\n");
            stats.append("本测试通过user_id.hashCode() % 4 进行分片\n");
            stats.append("数据库分片：ds0, ds1, ds2, ds3\n");
            stats.append("表分片：t_order_0, t_order_1, t_order_2, t_order_3\n");
            stats.append("主键生成：使用自定义KeyGenerator集成nextera-id服务\n");
            stats.append("\n请检查数据库中的数据分布情况：\n");
            stats.append("1. 查看不同数据库中的订单分布\n");
            stats.append("2. 查看不同表中的数据分布\n");
            stats.append("3. 验证主键是否由ID服务生成\n");
            
            return Result.success(stats.toString());
        } catch (Exception e) {
            log.error("获取分片统计失败", e);
            return Result.error("获取统计失败: " + e.getMessage());
        }
    }

    @Operation(summary = "清理测试数据", description = "清理测试产生的数据")
    @DeleteMapping("/cleanup")
    public Result<String> cleanup() {
        log.info("清理测试数据功能需要手动执行SQL");
        
        StringBuilder sql = new StringBuilder();
        sql.append("-- 清理测试数据SQL --\n");
        for (int db = 0; db < 4; db++) {
            sql.append("-- 数据库 order_db_").append(db).append("\n");
            for (int table = 0; table < 4; table++) {
                sql.append("DELETE FROM order_db_").append(db).append(".t_order_").append(table)
                   .append(" WHERE remark LIKE '%分库分表测试订单%';\n");
                sql.append("DELETE FROM order_db_").append(db).append(".t_order_item_").append(table)
                   .append(" WHERE order_id IN (SELECT id FROM order_db_").append(db).append(".t_order_").append(table)
                   .append(" WHERE remark LIKE '%分库分表测试订单%');\n");
            }
        }
        
        return Result.success(sql.toString());
    }
} 