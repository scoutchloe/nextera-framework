#!/bin/bash

# RocketMQ Broker 启动脚本
# 适用于 Nextera Framework 项目

set -e

# 设置基本环境变量
export ROCKETMQ_HOME=/home/rocketmq
export JAVA_HOME=${JAVA_HOME:-/usr/lib/jvm/java-1.8-openjdk}

# 设置 RocketMQ 相关环境变量
export CLASSPATH=.:${ROCKETMQ_HOME}/lib/*:${CLASSPATH}
export ROCKETMQ_HOME

# 创建必要的目录
echo "Creating necessary directories..."
mkdir -p /home/rocketmq/store
mkdir -p /home/rocketmq/logs
mkdir -p /home/rocketmq/store/commitlog
mkdir -p /home/rocketmq/store/consumequeue
mkdir -p /home/rocketmq/store/index

# 设置目录权限
chown -R rocketmq:rocketmq /home/rocketmq/store /home/rocketmq/logs 2>/dev/null || true

# 打印环境信息
echo "============================================"
echo "RocketMQ Broker Starting..."
echo "ROCKETMQ_HOME: $ROCKETMQ_HOME"
echo "NAMESRV_ADDR: $NAMESRV_ADDR"
echo "JAVA_HOME: $JAVA_HOME"
echo "Container: nextera-rocketmq-broker"
echo "============================================"

# 等待 NameServer 就绪
echo "Waiting for NameServer to be ready..."
while ! nc -z rocketmq-nameserver 9876; do
    echo "Waiting for NameServer..."
    sleep 2
done
echo "NameServer is ready!"

# 使用配置文件启动 Broker
echo "Starting RocketMQ Broker with configuration file..."
exec sh mqbroker -c /opt/rocketmq/conf/broker.conf 