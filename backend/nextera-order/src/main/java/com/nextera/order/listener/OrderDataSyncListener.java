package com.nextera.order.listener;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.nextera.order.document.OrderDocument;
import com.nextera.order.document.OrderItemDocument;
import com.nextera.order.entity.Order;
import com.nextera.order.entity.OrderItem;
import com.nextera.order.repository.OrderDocumentRepository;
import com.nextera.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 订单数据同步监听器
 * 监听Canal发送的RocketMQ消息，将数据同步到ElasticSearch
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rocketmq.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
@RocketMQMessageListener(
        topic = "nextera-order-sync",
        consumerGroup = "nextera-order-sync-consumer",
        consumeMode = ConsumeMode.CONCURRENTLY,
        messageModel = MessageModel.CLUSTERING
)
public class OrderDataSyncListener implements RocketMQListener<String> {

    private final OrderDocumentRepository orderDocumentRepository;
    private final OrderService orderService;

    @Override
    public void onMessage(String message) {
        try {
            log.info("接收到Canal数据同步消息: {}", message);
            
            // 解析Canal消息
            JSONObject canalMessage = JSON.parseObject(message);
            String database = canalMessage.getString("database");
            String table = canalMessage.getString("table");
            String type = canalMessage.getString("type");
            
            // 只处理order_db_x数据库的订单相关表
            if (!database.matches("order_db_[0-3]")) {
                log.debug("忽略非订单数据库的消息: {}", database);
                return;
            }
            
            // 根据表名和操作类型处理数据（支持分表）
            if (table.matches("t_order_[0-3]")) {
                handleOrderSync(canalMessage, type);
            } else if (table.matches("t_order_item_[0-3]")) {
                handleOrderItemSync(canalMessage, type);
            } else {
                log.debug("忽略非订单相关表的消息: {}", table);
            }
            
        } catch (Exception e) {
            log.error("处理Canal数据同步消息失败: {}", message, e);
            // 这里可以添加失败重试或死信队列逻辑
        }
    }

    /**
     * 处理订单表数据同步
     */
    private void handleOrderSync(JSONObject canalMessage, String eventType) {
        try {
            String database = canalMessage.getString("database");
            String table = canalMessage.getString("table");
            List<Map> data = canalMessage.getList("data",  Map.class);
//            @SuppressWarnings("unchecked")
//            List<Map<String, Object>> data = (List<Map<String, Object>>) rawData;
            
            for (Map<String, Object> row : data) {
                Long orderId = Long.valueOf(row.get("id").toString());
                
                switch (eventType.toLowerCase()) {
                    case "insert":
                    case "update":
                        syncOrderToElasticsearch(orderId, eventType, database, table);
                        break;
                    case "delete":
                        deleteOrderFromElasticsearch(orderId);
                        break;
                    default:
                        log.warn("未知的事件类型: {}", eventType);
                        break;
                }
            }
        } catch (Exception e) {
            log.error("处理订单同步失败", e);
        }
    }

    /**
     * 处理订单明细表数据同步
     */
    private void handleOrderItemSync(JSONObject canalMessage, String eventType) {
        try {
            String database = canalMessage.getString("database");
            String table = canalMessage.getString("table");
            List<Map> data = canalMessage.getList("data", Map.class);
//            @SuppressWarnings("unchecked")
//            List<Map<String, Object>> data = (List<Map<String, Object>>) rawData;
            
            for (Map<String, Object> row : data) {
                Long orderId = Long.valueOf(row.get("order_id").toString());
                
                // 订单明细变更时，需要重新同步整个订单到ES
                switch (eventType.toLowerCase()) {
                    case "insert":
                    case "update":
                    case "delete":
                        syncOrderToElasticsearch(orderId, eventType, database, table);
                        break;
                    default:
                        log.warn("未知的事件类型: {}", eventType);
                        break;
                }
            }
        } catch (Exception e) {
            log.error("处理订单明细同步失败", e);
        }
    }

    /**
     * 同步订单到ElasticSearch
     */
    private void syncOrderToElasticsearch(Long orderId, String eventType, String database, String table) {
        try {
            // 从数据库查询完整的订单信息
            Order order = orderService.getById(orderId);
            if (order == null) {
                log.warn("未找到订单: {}", orderId);
                return;
            }

            // 查询订单明细
            List<OrderItem> orderItems = orderService.getOrderItemsByOrderId(orderId);

            // 转换为ES文档
            OrderDocument orderDocument = convertToOrderDocument(order, orderItems, eventType, database, table);
            
            // 保存到ES
            orderDocumentRepository.save(orderDocument);
            log.info("成功同步订单到ES: orderId={}, eventType={}, database={}, table={}", orderId, eventType, database, table);
            
        } catch (Exception e) {
            log.error("同步订单到ES失败: orderId={}, database={}, table={}", orderId, database, table, e);
        }
    }

    /**
     * 从ElasticSearch删除订单
     */
    private void deleteOrderFromElasticsearch(Long orderId) {
        try {
            if (orderDocumentRepository.existsById(orderId)) {
                orderDocumentRepository.deleteById(orderId);
                log.info("成功从ES删除订单: orderId={}", orderId);
            } else {
                log.warn("ES中不存在订单: orderId={}", orderId);
            }
        } catch (Exception e) {
            log.error("从ES删除订单失败: orderId={}", orderId, e);
        }
    }

    /**
     * 转换为ES文档对象
     */
    private OrderDocument convertToOrderDocument(Order order, List<OrderItem> orderItems, String eventType, String database, String table) {
        // 转换订单明细
        List<OrderItemDocument> itemDocuments = new ArrayList<>();
        if (orderItems != null) {
            for (OrderItem item : orderItems) {
                OrderItemDocument itemDoc = OrderItemDocument.builder()
                        .id(item.getId())
                        .orderId(item.getOrderId())
                        .userId(item.getUserId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .productPrice(item.getProductPrice())
                        .quantity(item.getQuantity())
                        .totalPrice(item.getTotalPrice())
                        .createdTime(item.getCreatedTime())
                        .build();
                itemDocuments.add(itemDoc);
            }
        }

        // 转换订单
        return OrderDocument.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .userId(order.getUserId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .remark(order.getRemark())
                .createdTime(order.getCreatedTime())
                .updatedTime(order.getUpdatedTime())
                .orderItems(itemDocuments)
                .syncTime(LocalDateTime.now())
                .eventType(eventType)
                .database(database)
                .table(table)
                .build();
    }
} 