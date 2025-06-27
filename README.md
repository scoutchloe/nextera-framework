# Nextera Framework

## 项目概述

Nextera Framework 是一个基于 Spring Cloud 2025.0.0 的现代化微服务开发框架，采用前后端分离架构，提供完整的用户管理、订单管理，文章管理等功能模块。

## 技术栈

### 后端技术栈
- **Spring Cloud**: 2025.0.0
- **Spring Boot**: 3.5.0
- **构建工具**: Maven
- **注册中心**: Nacos2.4
- **seata**： 2.3.0
- **配置中心**: Nacos2.4
- **数据同步**：Canal 1.1.8
- **服务调用**: OpenFeign + Dubbo
- **ORM框架**: MyBatis-Plus
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **消息队列**: RocketMQ 5.1.3
- **搜索引擎**: Elasticsearch 8.18
- **监控**: Prometheus + Grafana
- **工具库**: Lombok, Hutool
- **API文档**: Knife4j

### 前端技术栈
- **构建工具**: Vite
- **框架**: Vue3
- **语言**: TypeScript
- **UI组件**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router
- **HTTP客户端**: Axios

## 项目结构

```
├── backend                 #springCloud 后端
│   ├── nextera-api         # api 模块
│   ├── nextera-article     # 文章模块 
│   ├── nextera-auth        # 认证模块
│   ├── nextera-common      # 通用模块
│   ├── nextera-gateway     # 网关
│   ├── nextera-id          # 自增长id 生成器模块
│   ├── nextera-id-api      # id-api 模块 
│   ├── nextera-order       # 分库分布订单模块 
│   ├── nextera-order-api   # 订单api 模块 
│   ├── nextera-user        # 用户模块 
│   └── pom.xml             # pom 
├── docker                  # docker 环境配置
│   ├── grafana
│   ├── mysql
│   ├── prometheus
│   ├── redis
│   └── rocketmq
├── docker-compose.yml      #docker 环境配置  
├── docker-compose.yml.backup
├── frontend
├── management              #管理端： 
│   ├── backend             # 管理端后端 ( ES 订单搜索)
│   └── frontend            # 管理端前端 (RBAC， 按钮级别权限控制,中英文切换)
└── src
    └── main
```

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.8+
- Node.js 18+
- Docker & Docker Compose

### 1. 启动基础设施

```bash
# 启动所有基础服务（MySQL、Redis、Nacos、RocketMQ等）
docker-compose up -d

# 查看服务状态
docker-compose ps
```

### 2. 服务地址

启动成功后，可以访问以下服务：

- **Nacos控制台**: http://localhost:8848/nacos (nacos/nacos)
- **网关服务**: http://localhost:8080
- **API文档**: http://localhost:8080/doc.html
- **RocketMQ控制台**: http://localhost:8180
- **Elasticsearch**: http://localhost:9200
- **Kibana**: http://localhost:5601
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin123)

### 3. 启动后端服务

```bash
cd backend

# 编译项目
mvn clean compile

# 启动网关服务
cd nextera-gateway
mvn spring-boot:run

# 启动用户服务
cd nextera-user
mvn spring-boot:run

# 启动其他服务...
```

### 4. 启动前端服务

```bash
# 用户端前端
cd frontend
npm install
npm run dev

# 管理端前端
cd management-frontend
npm install
npm run dev
```

## 开发指南

### 代码规范

1. **Java代码规范**
   - 遵循阿里巴巴Java开发手册
   - 使用统一的代码格式化配置
   - 添加必要的注释和文档

2. **前端代码规范**
   - 使用ESLint进行代码检查
   - 使用Prettier进行代码格式化
   - 使用TypeScript进行类型定义

### 项目命名规范

- **Maven模块**: nextera-xxx
- **Java包名**: com.nextera.xxx
- **API接口**: RESTful风格
- **数据库表**: 下划线分隔

### Git规范

- **分支管理**: 采用Git Flow模式
- **提交规范**: 遵循Conventional Commits
- **代码审查**: 通过Pull Request进行

## 核心特性

### 后端核心特性

1. **微服务架构**
   - 服务注册与发现
   - 负载均衡
   - 熔断降级
   - 分布式配置

2. **统一异常处理**
   - 全局异常处理器
   - 自定义异常类型
   - 统一错误码

3. **认证与授权**
   - JWT Token认证
   - RBAC权限模型
   - 多租户支持

4. **数据访问**
   - 多数据源支持
   - 读写分离
   - 分库分表
   - 缓存优化

5. **API安全**
   - 接口加密
   - 签名验证
   - 防重放攻击
   - 限流控制

### 前端核心特性

1. **现代化UI**
   - 科技感设计风格
   - 响应式布局
   - 暗黑模式支持
   - 动画效果

2. **权限控制**
   - 路由权限控制
   - 菜单权限控制
   - 按钮级别权限
   - 数据权限控制

3. **开发体验**
   - 热重载开发
   - TypeScript支持
   - 组件化开发
   - 状态管理

## 部署方案

### 开发环境
- 使用Docker Compose一键部署基础设施
- 支持本地开发环境配置
- 热重载开发支持

### 生产环境
- Kubernetes部署
- 服务监控告警
- 日志收集分析
- 自动化CI/CD

## 监控与运维

### 服务监控
- **Prometheus**: 指标收集
- **Grafana**: 监控大盘
- **Actuator**: 健康检查

### 日志管理
- **ELK Stack**: 日志收集分析
- **统一日志格式**
- **分布式链路追踪**

### 性能优化
- **缓存策略**
- **数据库优化**
- **接口性能监控**

## 文档资源

- [开发计划](./开发计划.md)
- [API文档](http://localhost:8080/doc.html)
- [架构设计文档](./docs/architecture.md)
- [部署指南](./docs/deployment.md)

## 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 联系方式

- 项目维护者: Nextera Team
- 邮箱: nextera@example.com
- 项目地址: https://github.com/nextera/nextera-framework

## 更新日志

### v1.0.0 (2025-06-16)
- 初始版本发布
- 完成基础架构搭建
- 实现用户管理功能
- 实现文章管理功能
- 完成前端界面开发 