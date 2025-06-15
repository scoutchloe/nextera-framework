#!/bin/bash

# RocketMQ 测试脚本
# 用于验证 RocketMQ 集群是否正常工作

set -e

echo "============================================"
echo "RocketMQ 配置测试脚本"
echo "============================================"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 测试函数
test_nameserver() {
    echo -e "${YELLOW}测试 NameServer 连接...${NC}"
    
    if docker exec nextera-rocketmq-broker sh mqadmin clusterList -n rocketmq-nameserver:9876 > /dev/null 2>&1; then
        echo -e "${GREEN}✓ NameServer 连接正常${NC}"
        return 0
    else
        echo -e "${RED}✗ NameServer 连接失败${NC}"
        return 1
    fi
}

test_broker() {
    echo -e "${YELLOW}测试 Broker 状态...${NC}"
    
    if docker exec nextera-rocketmq-broker sh mqadmin brokerStatus -n rocketmq-nameserver:9876 -b nextera-rocketmq-broker:10911 > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Broker 状态正常${NC}"
        return 0
    else
        echo -e "${RED}✗ Broker 状态异常${NC}"
        return 1
    fi
}

create_test_topic() {
    echo -e "${YELLOW}创建测试 Topic...${NC}"
    
    if docker exec nextera-rocketmq-broker sh mqadmin updateTopic -n rocketmq-nameserver:9876 -c DefaultCluster -t TestTopic -q 4 > /dev/null 2>&1; then
        echo -e "${GREEN}✓ 测试 Topic 创建成功${NC}"
        return 0
    else
        echo -e "${RED}✗ 测试 Topic 创建失败${NC}"
        return 1
    fi
}

test_console() {
    echo -e "${YELLOW}测试 Console 访问...${NC}"
    
    if curl -s http://localhost:8180/cluster/list.query > /dev/null 2>&1; then
        echo -e "${GREEN}✓ Console 访问正常${NC}"
        return 0
    else
        echo -e "${RED}✗ Console 访问失败${NC}"
        return 1
    fi
}

show_cluster_info() {
    echo -e "${YELLOW}显示集群信息...${NC}"
    echo "=========================================="
    
    echo -e "${YELLOW}集群列表：${NC}"
    docker exec nextera-rocketmq-broker sh mqadmin clusterList -n rocketmq-nameserver:9876 2>/dev/null || echo "获取集群信息失败"
    
    echo ""
    echo -e "${YELLOW}Topic 列表：${NC}"
    docker exec nextera-rocketmq-broker sh mqadmin topicList -n rocketmq-nameserver:9876 2>/dev/null || echo "获取 Topic 列表失败"
}

check_services() {
    echo -e "${YELLOW}检查 Docker 服务状态...${NC}"
    
    services=("nextera-rocketmq-nameserver" "nextera-rocketmq-broker" "nextera-rocketmq-console")
    
    for service in "${services[@]}"; do
        if docker ps | grep -q "$service"; then
            status=$(docker ps --format "table {{.Names}}\t{{.Status}}" | grep "$service" | awk '{print $2}')
            echo -e "${GREEN}✓ $service: $status${NC}"
        else
            echo -e "${RED}✗ $service: 未运行${NC}"
        fi
    done
}

# 主测试流程
main() {
    echo -e "${YELLOW}开始 RocketMQ 配置测试...${NC}"
    echo ""
    
    # 检查服务状态
    check_services
    echo ""
    
    # 等待服务启动完成
    echo -e "${YELLOW}等待服务启动完成...${NC}"
    sleep 10
    
    # 执行测试
    passed=0
    total=4
    
    test_nameserver && ((passed++)) || true
    test_broker && ((passed++)) || true
    create_test_topic && ((passed++)) || true
    test_console && ((passed++)) || true
    
    echo ""
    echo "=========================================="
    echo -e "${YELLOW}测试结果：${NC}"
    
    if [ $passed -eq $total ]; then
        echo -e "${GREEN}✓ 所有测试通过 ($passed/$total)${NC}"
        echo -e "${GREEN}RocketMQ 配置正确，可以正常使用！${NC}"
        
        echo ""
        show_cluster_info
        
        echo ""
        echo -e "${YELLOW}访问信息：${NC}"
        echo "- NameServer: localhost:9876"
        echo "- Broker: localhost:10911"
        echo "- Console: http://localhost:8180"
        
    else
        echo -e "${RED}✗ 部分测试失败 ($passed/$total)${NC}"
        echo -e "${RED}请检查 RocketMQ 配置和服务状态${NC}"
        exit 1
    fi
}

# 如果直接运行脚本
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi 