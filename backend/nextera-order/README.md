# Nextera-Order 订单模块

## 概述

nextera-order 是基于 Spring Boot + ShardingSphere + MyBatis Plus + Dubbo 构建的分布式订单管理模块，支持分库分表、分布式ID生成、微服务调用等功能。

## 主要特性

- **分库分表**: 支持4个数据库，每个数据库4张表，基于用户ID进行分片
- **分布式ID**: 集成nextera-id服务生成全局唯一ID  
- **微服务架构**: 支持REST API和Dubbo RPC两种调用方式
- **事务支持**: 支持分布式事务管理
- **状态管理**: 完整的订单状态流转控制
- **参数验证**: 完善的参数校验和异常处理

## 技术架构

### 分库分表设计

```
数据库分片: user_id.hashCode() % 4
- order_db_0
- order_db_1  
- order_db_2
- order_db_3

表分片: user_id.hashCode() % 4
- t_order_0, t_order_1, t_order_2, t_order_3
- t_order_item_0, t_order_item_1, t_order_item_2, t_order_item_3
```

### 核心依赖

- Spring Boot 3.5.0
- ShardingSphere JDBC 5.5.2
- MyBatis Plus 3.5.12
- Dubbo 3.3.0
- MySQL 8.0+

## 部署指南

### 1. 数据库初始化

执行数据库初始化脚本：

```bash
mysql -u root -p < nextera-order-database-init.sql
```

### 2. 配置修改

修改 `application-local.yml` 中的数据库连接配置：

```yaml
spring:
  shardingsphere:
    datasource:
      ds0:
        jdbc-url: jdbc:mysql://localhost:3306/order_db_0?...
        username: your_username
        password: your_password
      # 修改其他3个数据源配置...
```

### 3. 启动服务

```bash
cd backend/nextera-order
mvn spring-boot:run
```

服务启动后会运行在端口 8088。

## API 接口

### REST API

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/orders` | 创建订单 |
| GET | `/api/orders/{id}` | 根据ID查询订单 |
| GET | `/api/orders/orderNo/{orderNo}` | 根据订单号查询订单 |
| GET | `/api/orders` | 分页查询订单 |
| GET | `/api/orders/user/{userId}` | 查询用户订单 |
| PUT | `/api/orders/{id}/status` | 更新订单状态 |
| PUT | `/api/orders/{id}/cancel` | 取消订单 |

### 测试接口

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/test/orders/health` | 健康检查 |
| GET | `/api/test/orders/test-id-generator` | 测试ID生成器 |
| POST | `/api/test/orders/test-create-order` | 测试创建订单 |
| GET | `/api/test/orders/test-sharding` | 测试分库分表 |

### Dubbo RPC 接口

```java
// 服务接口：com.nextera.order.dubbo.OrderRpcService
// 版本：1.0.0
// 分组：default

Result<OrderResponse> createOrder(CreateOrderRequest request);
Result<OrderResponse> getOrderById(Long orderId);
Result<IPage<OrderResponse>> getUserOrders(Long userId, Integer current, Integer size);
Result<Boolean> updateOrderStatus(Long orderId, Integer status);
```

## 使用示例

### 1. 创建订单

```bash
curl -X POST http://localhost:8088/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1001,
    "paymentMethod": 1,
    "remark": "测试订单",
    "items": [
      {
        "productId": 2001,
        "productName": "商品1",
        "productPrice": 99.99,
        "quantity": 2
      }
    ]
  }'
```

### 2. 查询订单

```bash
curl http://localhost:8088/api/orders/1234567890
```

### 3. 测试分库分表

```bash
curl http://localhost:8088/api/test/orders/test-sharding
```

## 订单状态说明

| 状态码 | 状态名称 | 描述 |
|--------|----------|------|
| 1 | 待支付 | 订单已创建，等待支付 |
| 2 | 已支付 | 订单已支付 |
| 3 | 已发货 | 订单已发货 |
| 4 | 已完成 | 订单已完成 |
| 5 | 已取消 | 订单已取消 |

### 状态流转规则

- 待支付 → 已支付、已取消
- 已支付 → 已发货、已取消  
- 已发货 → 已完成
- 已完成/已取消 → 终态，不可变更

## 监控指标

访问：http://localhost:8088/actuator/health

可监控的指标：
- 应用健康状态
- 数据库连接状态
- Dubbo服务状态

## 接口文档

启动服务后访问：http://localhost:8088/doc.html

查看完整的 Swagger API 文档。

## 常见问题

### Q: 分库分表如何验证？

A: 可通过测试接口 `/api/test/orders/test-sharding` 来验证分库分表效果，该接口会创建多个用户的订单并显示分片信息。

### Q: ID生成服务调用失败怎么办？

A: 系统会自动降级到时间戳+随机数的备用方案，确保服务可用性。

### Q: 如何查看SQL执行情况？

A: 配置文件中已开启 SQL 日志，可在控制台查看实际执行的 SQL 语句。

## 开发计划

详细的开发计划和任务进度请参考：[nextera-order开发计划.md](../nextera-order开发计划.md)

## 技术支持

如有问题，请联系开发团队或查看项目文档。 