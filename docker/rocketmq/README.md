# RocketMQ 配置说明

## 概述

本目录包含 Nextera Framework 项目的 RocketMQ 配置文件。RocketMQ 是一个分布式消息中间件，支持高吞吐量、低延迟的消息传递。

## 文件说明

### 配置文件

- **broker.conf**: 生产环境 Broker 配置文件，包含详细的性能优化配置
- **broker-simple.conf**: 简化版 Broker 配置文件，适用于开发和测试环境
- **broker-4.9.conf**: 预留的 4.9 版本配置文件（当前未使用）

### 脚本文件

- **start-broker.sh**: Broker 启动脚本，包含环境检查和健康检查

## 配置特性

### 生产环境配置 (broker.conf)

1. **性能优化**
   - G1 垃圾收集器
   - 优化的线程池配置
   - 内存映射文件预热
   - 批量消息处理

2. **高可用性**
   - 异步主从复制
   - 自动故障转移
   - 健康检查机制

3. **存储优化**
   - 按时间删除过期文件
   - 磁盘使用率监控
   - 压缩和索引优化

4. **消息特性**
   - 延迟消息支持
   - 事务消息
   - 消息轨迹追踪
   - 死信队列

### 开发环境配置 (broker-simple.conf)

1. **简化配置**
   - 最基本的功能配置
   - 自动创建 Topic 和订阅组
   - 较少的队列数量

2. **快速启动**
   - 减少内存占用
   - 简化的延迟消息配置

## 使用方法

### 启动 RocketMQ

```bash
# 启动整个 RocketMQ 集群
docker-compose up -d rocketmq-nameserver rocketmq-broker rocketmq-console

# 查看日志
docker-compose logs -f rocketmq-nameserver
docker-compose logs -f rocketmq-broker
docker-compose logs -f rocketmq-console
```

### 访问控制台

RocketMQ Console 访问地址：http://localhost:8180

### 连接参数

- **NameServer**: localhost:9876
- **Broker**: localhost:10911
- **Console**: localhost:8180

## 监控和管理

### 健康检查

所有 RocketMQ 服务都配置了健康检查：

- **NameServer**: 每30秒检查一次
- **Broker**: 每30秒检查一次，启动后90秒开始
- **Console**: 每30秒检查一次，启动后60秒开始

### 日志查看

```bash
# 查看 NameServer 日志
docker exec -it nextera-rocketmq-nameserver tail -f /home/rocketmq/logs/rocketmqlogs/namesrv.log

# 查看 Broker 日志
docker exec -it nextera-rocketmq-broker tail -f /home/rocketmq/logs/rocketmqlogs/broker.log
```

### 管理命令

```bash
# 进入 Broker 容器
docker exec -it nextera-rocketmq-broker sh

# 查看集群状态
sh mqadmin clusterList -n rocketmq-nameserver:9876

# 查看 Topic 列表
sh mqadmin topicList -n rocketmq-nameserver:9876

# 创建 Topic
sh mqadmin updateTopic -n rocketmq-nameserver:9876 -c DefaultCluster -t TestTopic -q 8
```

## 故障排除

### 常见问题

1. **Broker 启动失败**
   - 检查 NameServer 是否正常运行
   - 检查网络连接
   - 查看 broker.log 日志

2. **消息发送失败**
   - 检查 Topic 是否存在
   - 检查 Broker 状态
   - 验证网络连接

3. **Console 无法访问**
   - 检查 Console 容器状态
   - 验证端口映射
   - 检查防火墙设置

### 日志位置

- **NameServer**: `/home/rocketmq/logs/rocketmqlogs/namesrv.log`
- **Broker**: `/home/rocketmq/logs/rocketmqlogs/broker.log`
- **Console**: 容器标准输出

## 性能调优

### JVM 参数优化

当前配置使用了 G1 垃圾收集器和以下优化参数：

- `-XX:+UseG1GC`: 使用 G1 垃圾收集器
- `-XX:G1HeapRegionSize=16m`: 设置 G1 堆区域大小
- `-XX:G1ReservePercent=25`: 设置 G1 保留百分比
- `-XX:InitiatingHeapOccupancyPercent=30`: 设置初始堆占用百分比

### 存储优化

- 使用 SSD 存储可提高性能
- 定期清理过期文件
- 监控磁盘使用率

### 网络优化

- 确保网络带宽充足
- 使用专用网络
- 配置合适的缓冲区大小

## 版本信息

- **RocketMQ 版本**: 5.1.3
- **Java 版本**: OpenJDK 8+
- **配置版本**: 2024.1

## 更新日志

- 2024.1: 初始版本，支持 RocketMQ 5.1.3
- 优化了生产环境配置
- 添加了健康检查和监控功能
- 支持延迟消息和事务消息 