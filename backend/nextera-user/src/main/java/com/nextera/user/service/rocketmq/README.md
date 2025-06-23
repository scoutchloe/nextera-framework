# RocketMQ事务消息分布式事务方案

## 概述

本方案基于RocketMQ的事务消息机制实现分布式事务，确保用户更新文章操作的数据一致性。相比TCC模式，RocketMQ事务消息提供了更简单的编程模型和更好的可运维性。

## 架构设计

### 核心组件

1. **UserArticleRocketMQService** - 事务消息发送服务
2. **ArticleUpdateTransactionListener** - 事务监听器，处理本地事务执行和回查
3. **ArticleUpdateMessageConsumer** - 消息消费者，处理文章更新
4. **RocketmqTransactionLog** - 事务日志表，记录事务状态
5. **UserArticleOperationLog** - 操作日志表，记录用户操作历史

### 数据库表

```sql
-- 事务消息日志表
rocketmq_transaction_log

-- 用户操作日志表  
user_article_operation_log

-- 补偿任务表（可选）
transaction_compensation_task
```

## 事务流程

### 1. 正常流程

```
用户请求 -> 发送半消息 -> 执行本地事务 -> 提交/回滚消息 -> 消费者处理 -> 调用文章服务
```

### 2. 详细步骤

1. **用户发起更新请求**
   - 调用 `/api/user/article/update-rocketmq` 接口
   - 传入文章ID、用户ID和更新内容

2. **发送RocketMQ半消息**
   - 构建 `ArticleUpdateMessageDTO` 消息体
   - 调用 `rocketMQTemplate.sendMessageInTransaction()` 发送半消息
   - 半消息对消费者不可见

3. **执行本地事务**
   - RocketMQ回调 `ArticleUpdateTransactionListener.executeLocalTransaction()`
   - 在本地数据库中：
     - 保存事务日志到 `rocketmq_transaction_log`
     - 保存操作日志到 `user_article_operation_log`
     - 更新用户最后登录时间
   - 返回事务执行结果

4. **提交或回滚消息**
   - 如果本地事务成功，RocketMQ将半消息转为正常消息
   - 如果本地事务失败，RocketMQ删除半消息

5. **消费者处理消息**
   - `ArticleUpdateMessageConsumer` 消费消息
   - 通过Feign调用文章服务更新文章
   - 更新事务状态和操作日志

### 3. 异常处理

#### 网络异常/超时场景
- RocketMQ会定期回查本地事务状态
- 调用 `ArticleUpdateTransactionListener.checkLocalTransaction()`
- 根据数据库中的事务状态决定提交或回滚

#### 消费失败场景
- 消费者抛出异常，触发RocketMQ重试机制
- 超过最大重试次数后，消息进入死信队列
- 可通过补偿机制处理死信消息

## 与TCC模式对比

| 特性 | TCC模式 | RocketMQ事务消息 |
|------|---------|------------------|
| 编程复杂度 | 高（需要实现Try/Confirm/Cancel） | 低（只需实现本地事务） |
| 性能 | 高（强一致性） | 中等（最终一致性） |
| 一致性 | 强一致性 | 最终一致性 |
| 运维复杂度 | 高 | 中等 |
| 数据冗余 | 低 | 中等（消息存储） |
| 故障恢复 | 复杂 | 简单（依赖MQ重试） |

## 使用方式

### 1. API调用

```bash
curl -X POST http://localhost:8080/api/user/article/update-rocketmq \
  -H "Content-Type: application/json" \
  -d '{
    "title": "更新的文章标题",
    "content": "更新的文章内容",
    "categoryId": 1,
    "summary": "文章摘要",
    "tags": "标签1,标签2",
    "isPublished": 1
  }' \
  -G -d "userId=1001" -d "articleId=2001"
```

### 2. 事务状态查询

```sql
-- 查询事务日志
SELECT * FROM rocketmq_transaction_log WHERE transaction_id = 'TXN_1001_2001_xxx';

-- 查询操作日志
SELECT * FROM user_article_operation_log WHERE transaction_id = 'TXN_1001_2001_xxx';
```

## 监控和运维

### 1. 关键指标监控

- 事务消息发送成功率
- 本地事务执行成功率
- 消息消费成功率
- 回查频率和成功率

### 2. 日志关键字

```
# 事务开始
"开始RocketMQ事务消息处理文章更新"

# 本地事务执行
"执行RocketMQ本地事务"

# 事务回查
"RocketMQ回查本地事务状态"

# 消息消费
"接收到文章更新消息"
```

### 3. 故障排查

1. **消息发送失败**
   - 检查RocketMQ连接状态
   - 查看生产者配置和网络

2. **本地事务失败**
   - 检查数据库连接
   - 查看事务日志表状态

3. **消息消费失败**
   - 检查消费者日志
   - 查看死信队列消息

## 扩展功能

### 1. 补偿任务
- 定时任务扫描失败的事务
- 自动或手动触发补偿操作

### 2. 事务可视化
- 提供Web界面查看事务状态
- 支持手动干预和重试

### 3. 指标统计
- 事务成功率统计
- 性能监控和报警

## 注意事项

1. **幂等性**: 消费者必须保证处理的幂等性
2. **消息顺序**: 如需保证顺序，使用顺序消息
3. **事务超时**: 合理设置事务超时时间
4. **消息大小**: 注意消息体大小限制
5. **重试策略**: 根据业务需求配置重试次数和间隔

## 最佳实践

1. **事务边界**: 保持本地事务简单且快速
2. **错误处理**: 完善的异常处理和日志记录
3. **监控告警**: 建立完善的监控体系
4. **数据清理**: 定期清理过期的事务日志
5. **性能优化**: 根据业务量调整RocketMQ参数 