# Nextera Order API

## 概述

`nextera-order-api` 是订单服务的API接口模块，提供对外的Dubbo服务接口定义、DTO传输对象和业务枚举。该模块遵循API分离原则，确保接口定义的稳定性和复用性。

## 模块结构

```
nextera-order-api/
├── src/main/java/com/nextera/orderapi/
│   ├── dto/                    # 数据传输对象
│   │   ├── CreateOrderRequest.java
│   │   ├── OrderResponse.java
│   │   └── OrderQueryRequest.java
│   ├── enums/                  # 业务枚举
│   │   ├── OrderStatus.java
│   │   └── PaymentMethod.java
│   └── service/                # 服务接口
│       └── OrderRpcService.java
└── pom.xml
```

## 核心组件

### 1. DTO 传输对象

#### CreateOrderRequest
- 创建订单请求对象
- 包含用户ID、支付方式、订单备注和订单明细列表
- 支持参数验证注解

#### OrderResponse
- 订单响应对象
- 包含完整的订单信息和明细列表
- 支持状态和支付方式的描述展示

#### OrderQueryRequest
- 订单查询请求对象
- 支持分页查询和多条件筛选
- 包含订单号、用户ID、状态等查询条件

### 2. 业务枚举

#### OrderStatus
- 订单状态枚举：待支付、已支付、已发货、已完成、已取消
- 提供状态流转验证逻辑
- 支持状态码与枚举的双向转换

#### PaymentMethod
- 支付方式枚举：微信支付、支付宝、银行卡
- 提供支付方式描述
- 支持支付方式码与枚举的双向转换

### 3. 服务接口

#### OrderRpcService
- 订单RPC服务接口
- 提供创建订单、查询订单、更新状态等核心业务接口
- 支持分页查询和状态管理

## 使用方式

### 1. 依赖引入

在需要使用订单API的模块中引入依赖：

```xml
<dependency>
    <groupId>com.nextera</groupId>
    <artifactId>nextera-order-api</artifactId>
</dependency>
```

### 2. 服务消费

#### Dubbo服务消费
```java
@Service
public class OrderConsumerService {
    
    @DubboReference(version = "1.0.0", group = "default")
    private OrderRpcService orderRpcService;
    
    public OrderResponse createOrder(CreateOrderRequest request) {
        Result<OrderResponse> result = orderRpcService.createOrder(request);
        return result.getData();
    }
}
```

#### DTO使用示例
```java
// 创建订单请求
CreateOrderRequest request = new CreateOrderRequest();
request.setUserId(1001L);
request.setPaymentMethod(PaymentMethod.WECHAT.getCode());
request.setRemark("测试订单");

// 添加订单明细
List<CreateOrderRequest.OrderItemDto> items = new ArrayList<>();
CreateOrderRequest.OrderItemDto item = new CreateOrderRequest.OrderItemDto();
item.setProductId(2001L);
item.setProductName("商品A");
item.setProductPrice(new BigDecimal("99.99"));
item.setQuantity(2);
items.add(item);

request.setItems(items);
```

## 设计原则

1. **接口稳定性**：API模块保持接口的稳定性，避免频繁变更
2. **数据封装**：通过DTO对象封装数据传输，保护内部实体结构
3. **参数验证**：使用Bean Validation进行参数校验
4. **枚举规范**：统一业务枚举定义，提供描述和转换方法
5. **版本管理**：通过版本号管理API兼容性

## 版本信息

- **当前版本**：1.0.0
- **Spring Boot版本**：3.2.0
- **Dubbo版本**：3.2.7
- **Java版本**：17+

## 注意事项

1. 该模块仅包含接口定义，不包含具体实现
2. 修改API接口时需要考虑向后兼容性
3. 新增接口方法时需要更新文档
4. 枚举类型变更需要确保与数据库字段一致 