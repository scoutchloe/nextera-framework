# TCC分布式事务说明文档

## 概述

TCC（Try-Confirm-Cancel）是一种分布式事务模式，通过三个阶段来保证分布式系统的数据一致性：

- **Try阶段**：预留资源，检查业务合法性，但不执行实际的业务逻辑
- **Confirm阶段**：确认执行业务操作，提交事务
- **Cancel阶段**：撤销业务操作，回滚事务

## 实现架构

### 主要组件

1. **UserArticleBizService** - 业务入口服务
   - `updateArticle()` - 委托给TCC服务处理
   - `updateArticleAT()` - 保留的AT模式实现

2. **UserArticleBizTCCService** - TCC分布式事务实现
   - `updateArticleTCC()` - TCC事务入口方法
   - `tryUpdateArticle()` - Try阶段实现
   - `confirmUpdateArticle()` - Confirm阶段实现
   - `cancelUpdateArticle()` - Cancel阶段实现

### 类结构

```java
@LocalTCC
public class UserArticleBizTCCService {
    
    @TwoPhaseBusinessAction(
        name = "updateArticleTryPhase",
        commitMethod = "confirmUpdateArticle", 
        rollbackMethod = "cancelUpdateArticle"
    )
    public boolean tryUpdateArticle(...);
    
    public boolean confirmUpdateArticle(BusinessActionContext context);
    
    public boolean cancelUpdateArticle(BusinessActionContext context);
}
```

## API接口

### 1. TCC模式更新文章（默认）
```
POST /api/user/article/update
Content-Type: application/json

{
    "title": "文章标题",
    "content": "文章内容",
    "categoryId": 1
}

Query Parameters:
- userId: 用户ID
- articleId: 文章ID
```

### 2. AT模式更新文章（保留）
```
POST /api/user/article/update-at
```

### 3. 直接TCC模式更新文章
```
POST /api/user/article/update-tcc
```

## TCC事务流程

### Try阶段
1. 验证用户是否存在
2. 验证文章是否存在且有权限
3. 创建TCC资源对象
4. 将资源信息保存到本地映射中
5. **不执行实际的业务修改**

### Confirm阶段
1. 从资源映射中获取预留的资源
2. 检查幂等性（防止重复执行）
3. 更新用户最后活动时间
4. 更新文章内容
5. 标记操作为已执行
6. 清理资源映射

### Cancel阶段
1. 从资源映射中获取预留的资源
2. **完整的数据补偿操作**：
   - 检查用户数据修改状态，如已修改则恢复最后登录时间到原始值
   - 检查文章数据修改状态，如已修改则恢复文章标题、内容、分类到原始值
   - 记录补偿操作日志，便于审计和监控
3. 清理资源映射
4. 确保幂等性，避免无限重试

## 关键技术点

### 1. 注解配置
```java
@LocalTCC                    // 标识TCC资源
@TwoPhaseBusinessAction(     // 定义两阶段业务动作
    name = "updateArticleTryPhase",
    commitMethod = "confirmUpdateArticle", 
    rollbackMethod = "cancelUpdateArticle"
)
```

### 2. 参数传递
```java
// Try阶段只能传递基本类型参数
@BusinessActionContextParameter(paramName = "articleId") Long articleId
@BusinessActionContextParameter(paramName = "title") String title
```

### 3. 幂等性保证
```java
// 防止重复执行
if (resource.isExecuted()) {
    log.info("TCC Confirm阶段 - 操作已执行，跳过，XID: {}", xid);
    return true;
}
```

### 4. 资源管理
```java
// 使用XID作为key管理事务资源，包含原始数据用于补偿
private final Map<String, TccResource> tccResourceMap = new ConcurrentHashMap<>();

// TCC资源包含原始数据和修改状态标记
public static class TccResource {
    // 新数据
    private final String title, content;
    private final Long categoryId;
    
    // 原始数据（用于补偿）
    private final String originalTitle, originalContent;
    private final Long originalCategoryId;
    private final LocalDateTime originalLastLoginTime;
    
    // 修改状态标记
    private boolean userDataModified = false;
    private boolean articleDataModified = false;
}
```

## 测试验证

### 1. 正常流程测试
```bash
curl -X POST "http://localhost:8083/api/user/article/update?userId=1&articleId=6" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "TCC测试文章",
    "content": "这是TCC模式测试内容",
    "categoryId": 1
  }'
```

### 2. 日志验证
关注以下关键日志：

**正常流程：**
- `TCC Try阶段开始 - 预留资源`
- `TCC Try阶段 - 保存原始数据，文章标题: 原标题 -> 新标题`
- `TCC Try阶段成功 - 预留资源完成`  
- `TCC Confirm阶段开始 - 确认执行业务操作`
- `TCC Confirm阶段 - 用户活动时间更新成功`
- `TCC Confirm阶段 - 文章更新成功`
- `TCC Confirm阶段成功 - 业务操作完成`

**异常回滚：**
- `TCC Cancel阶段开始 - 撤销业务操作`
- `TCC Cancel阶段 - 检测到用户数据已修改，开始恢复用户最后登录时间`
- `TCC Cancel阶段 - 用户数据补偿成功，恢复到: 原始时间`
- `TCC Cancel阶段 - 检测到文章数据已修改，开始恢复文章原始内容`
- `TCC Cancel阶段 - 文章数据补偿成功，恢复标题: 原标题`
- `TCC Cancel阶段成功 - 所有补偿操作完成`

### 3. 异常场景测试
- 用户不存在：返回"用户不存在"错误
- 文章不存在：Try阶段失败，触发Cancel
- 网络异常：触发Cancel阶段，回滚事务
- **补偿操作测试**：
  - 模拟Confirm阶段部分成功（用户数据更新成功，文章更新失败）
  - 验证Cancel阶段是否正确恢复已修改的用户数据
  - 检查补偿日志记录是否完整

## 故障排查

### 1. Confirm阶段不执行
**可能原因：**
- TCC注解配置错误
- 方法签名不匹配
- Spring代理问题
- Seata配置问题

**解决方案：**
- 检查`@LocalTCC`和`@TwoPhaseBusinessAction`注解
- 确保Confirm/Cancel方法为public
- 验证方法名与注解中的名称一致
- 检查Seata配置和注册中心连接

### 2. 资源泄漏
**现象：**
- `tccResourceMap`持续增长
- 内存占用异常

**解决方案：**
- 确保finally块中清理资源
- 添加定时清理过期资源的机制
- 监控资源映射大小

### 3. 幂等性问题
**现象：**
- 重复执行业务操作
- 数据不一致

**解决方案：**
- 使用`executed`标记防止重复执行
- 在Confirm/Cancel阶段检查资源状态

## 配置说明

### application.yml
```yaml
seata:
  enabled: true
  application-id: nextera-user
  tx-service-group: nextera-tx-group
  service:
    vgroup-mapping:
      nextera-tx-group: default
    grouplist:
      default: 127.0.0.1:8091
```

## 监控指标

- TCC事务成功率
- Try/Confirm/Cancel阶段耗时
- 资源映射大小
- 异常重试次数
- 事务超时次数

## 补偿操作测试

### 1. 模拟补偿场景
```bash
# 1. 正常更新文章，观察Try->Confirm流程
curl -X POST "http://localhost:8083/api/user/article/update?userId=1&articleId=6" \
  -H "Content-Type: application/json" \
  -d '{"title": "新标题", "content": "新内容", "categoryId": 2}'

# 2. 在Confirm阶段模拟异常，触发Cancel补偿
# 可以通过以下方式：
# - 关闭文章服务，模拟远程调用失败
# - 修改数据库权限，模拟数据库操作失败
# - 使用断点调试，在Confirm阶段抛出异常
```

### 2. 验证补偿效果
```sql
-- 查看用户最后登录时间是否恢复
SELECT id, username, last_login_time FROM user WHERE id = 1;

-- 查看文章内容是否恢复
SELECT id, title, content, category_id FROM article WHERE id = 6;
```

### 3. 补偿日志监控
```bash
# 查看补偿操作日志
grep "TCC补偿日志" application.log
grep "TCC Cancel阶段" application.log
```

## 最佳实践

1. **资源预留**：Try阶段只做验证和资源预留，不执行实际业务
2. **原始数据保存**：在Try阶段保存所有需要修改的数据的原始值
3. **状态标记**：使用标记位记录哪些数据已被修改，精确控制补偿范围
4. **幂等设计**：Confirm和Cancel阶段必须支持重复调用
5. **补偿策略**：Cancel阶段应该总是返回true，避免无限重试
6. **监控告警**：监控TCC事务的各个阶段执行情况和补偿操作成功率
7. **日志记录**：详细记录补偿操作，便于问题排查和数据审计
8. **超时处理**：设置合理的事务超时时间
9. **错误处理**：完善的异常处理和降级策略

## 性能考虑

- TCC模式相比AT模式有更高的业务侵入性
- 需要额外的资源管理开销
- 适用于对一致性要求极高的核心业务场景
- 建议在高并发场景下进行充分的性能测试 