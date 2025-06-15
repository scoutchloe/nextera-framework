#!/bin/bash

# Nextera Framework 启动脚本
# 用于快速启动项目的所有服务

echo "==========================================="
echo "      Nextera Framework 启动脚本"
echo "==========================================="

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo "❌ Docker 未安装，请先安装 Docker"
    exit 1
fi

# 检查Docker Compose是否安装
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose 未安装，请先安装 Docker Compose"
    exit 1
fi

# 检查Maven是否安装
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven 未安装，请先安装 Maven"
    exit 1
fi

# 检查Java是否安装
# if ! command -v java &> /dev/null; then
#     echo "❌ Java 未安装，请先安装 JDK 17+"
#     exit 1
# fi

echo "✅ 环境检查通过"

# 创建必要的目录
echo "📁 创建必要的目录..."
mkdir -p docker/mysql/conf
mkdir -p docker/mysql/init
mkdir -p docker/redis
mkdir -p docker/rocketmq
mkdir -p docker/prometheus
mkdir -p logs

# 创建Redis配置文件
echo "⚙️  创建Redis配置文件..."
cat > docker/redis/redis.conf << EOF
# Redis配置文件
port 6379
bind 0.0.0.0
protected-mode no
save 900 1
save 300 10
save 60 10000
rdbcompression yes
dbfilename dump.rdb
dir /data
appendonly yes
appendfsync everysec
EOF

# 创建MySQL配置文件
echo "⚙️  创建MySQL配置文件..."
cat > docker/mysql/conf/my.cnf << EOF
[mysqld]
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
init_connect='SET NAMES utf8mb4'
skip-character-set-client-handshake
max_connections=1000
max_connect_errors=1000
table_open_cache=1024
max_allowed_packet=128M
EOF

# 复制数据库初始化脚本
echo "⚙️  复制数据库初始化脚本..."
if [ -f "sql/nextera_init.sql" ]; then
    cp sql/nextera_init.sql docker/mysql/init/
    echo "✅ 数据库初始化脚本已复制"
else
    echo "⚠️  数据库初始化脚本不存在，创建基础脚本..."
    cat > docker/mysql/init/init.sql << EOF
-- 创建Nacos数据库
CREATE DATABASE IF NOT EXISTS nacos DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建认证服务数据库
CREATE DATABASE IF NOT EXISTS nextera_auth DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户服务数据库
CREATE DATABASE IF NOT EXISTS nextera_user DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建文章服务数据库
CREATE DATABASE IF NOT EXISTS nextera_article DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建管理服务数据库
CREATE DATABASE IF NOT EXISTS nextera_manage DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户并授权
CREATE USER IF NOT EXISTS 'nextera'@'%' IDENTIFIED BY 'nextera123';
GRANT ALL PRIVILEGES ON nextera_auth.* TO 'nextera'@'%';
GRANT ALL PRIVILEGES ON nextera_user.* TO 'nextera'@'%';
GRANT ALL PRIVILEGES ON nextera_article.* TO 'nextera'@'%';
GRANT ALL PRIVILEGES ON nextera_manage.* TO 'nextera'@'%';

FLUSH PRIVILEGES;
EOF
fi

# 创建RocketMQ配置文件
echo "⚙️  创建RocketMQ配置文件..."
cat > docker/rocketmq/broker.conf << EOF
brokerClusterName=DefaultCluster
brokerName=broker-a
brokerId=0
deleteWhen=04
fileReservedTime=120
brokerRole=ASYNC_MASTER
flushDiskType=ASYNC_FLUSH
brokerIP1=localhost
EOF

# 创建Prometheus配置文件
echo "⚙️  创建Prometheus配置文件..."
cat > docker/prometheus/prometheus.yml << EOF
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  - job_name: 'nextera-gateway'
    static_configs:
      - targets: ['host.docker.internal:8080']
    metrics_path: '/actuator/prometheus'

  - job_name: 'nextera-user'
    static_configs:
      - targets: ['host.docker.internal:8081']
    metrics_path: '/actuator/prometheus'

  - job_name: 'nextera-article'
    static_configs:
      - targets: ['host.docker.internal:8082']
    metrics_path: '/actuator/prometheus'
EOF

echo "🚀 启动基础设施服务..."

# 启动Docker Compose服务
docker-compose up -d

echo "⏳ 等待服务启动完成..."
sleep 30

# 检查服务状态
echo "📊 检查服务状态..."
docker-compose ps

echo ""
echo "✅ 基础设施服务启动完成！"
echo ""
echo "🌐 服务访问地址："
echo "   - Nacos控制台: http://localhost:8848/nacos (用户名/密码: nacos/nacos)"
echo "   - RocketMQ控制台: http://localhost:8180"
echo "   - Kibana: http://localhost:5601"
echo "   - Prometheus: http://localhost:9090"
echo "   - Grafana: http://localhost:3000 (用户名/密码: admin/admin123)"
echo ""

# 提示用户下一步操作
echo "📝 下一步操作："
echo "   1. 等待Nacos和MySQL完全启动后，可以开始启动微服务"
echo "   2. cd backend && mvn clean compile"
echo "   3. 启动认证服务: cd nextera-auth && mvn spring-boot:run"
echo "   4. 启动用户服务: cd nextera-user && mvn spring-boot:run"
echo "   5. 启动网关服务: cd nextera-gateway && mvn spring-boot:run"
echo "   6. 启动前端项目: cd frontend && npm install && npm run dev"
echo ""
echo "🔧 API测试地址："
echo "   - 认证服务API文档: http://localhost:8083/doc.html"
echo "   - 用户服务API文档: http://localhost:8081/doc.html" 
echo "   - 网关访问地址: http://localhost:8080"
echo ""
echo "👤 测试账户："
echo "   - 管理员: admin / admin123"
echo "   - 普通用户: user / admin123"
echo ""
echo "🎉 Nextera Framework 基础环境搭建完成！" 