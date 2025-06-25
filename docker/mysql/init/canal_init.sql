-- Canal 数据库初始化脚本
-- 创建时间: 2024-01-01
-- 版本: 1.0.0

-- 创建 Canal Admin 管理数据库
CREATE DATABASE IF NOT EXISTS canal_manager DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;

-- 创建 Canal TSDB 数据库（用于存储表结构信息）
CREATE DATABASE IF NOT EXISTS canal_tsdb DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;

-- 创建 Nextera 业务数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS nextera DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;

-- 切换到业务数据库
USE nextera;

-- 创建订单表
DROP TABLE IF EXISTS t_order;
CREATE TABLE t_order (
    id BIGINT PRIMARY KEY COMMENT '订单ID',
    order_no VARCHAR(64) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    status INT NOT NULL DEFAULT 1 COMMENT '订单状态(1:待支付,2:已支付,3:已发货,4:已完成,5:已取消)',
    payment_method INT COMMENT '支付方式(1:微信,2:支付宝,3:银行卡)',
    remark VARCHAR(500) COMMENT '订单备注',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_order_no (order_no),
    INDEX idx_status (status),
    INDEX idx_created_time (created_time)
) COMMENT='订单表';

-- 创建订单明细表
DROP TABLE IF EXISTS t_order_item;
CREATE TABLE t_order_item (
    id BIGINT PRIMARY KEY COMMENT '明细ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    user_id BIGINT NOT NULL COMMENT '用户ID（分片键）',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    product_price DECIMAL(10,2) NOT NULL COMMENT '商品单价',
    quantity INT NOT NULL COMMENT '购买数量',
    total_price DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_order_id (order_id),
    INDEX idx_user_id (user_id),
    INDEX idx_product_id (product_id),
    FOREIGN KEY (order_id) REFERENCES t_order(id)
) COMMENT='订单明细表';

-- 插入测试数据
INSERT INTO t_order (id, order_no, user_id, total_amount, status, payment_method, remark) VALUES
(1, 'ORD202401010001', 1001, 299.99, 2, 1, '测试订单1'),
(2, 'ORD202401010002', 1002, 159.99, 1, 2, '测试订单2'),
(3, 'ORD202401010003', 1003, 89.99, 4, 1, '测试订单3');

INSERT INTO t_order_item (id, order_id, user_id, product_id, product_name, product_price, quantity, total_price) VALUES
(1, 1, 1001, 2001, '商品A', 99.99, 2, 199.98),
(2, 1, 1001, 2002, '商品B', 100.01, 1, 100.01),
(3, 2, 1002, 2003, '商品C', 159.99, 1, 159.99),
(4, 3, 1003, 2004, '商品D', 89.99, 1, 89.99);

-- 切换到 Canal Manager 数据库
USE canal_manager;

-- Canal Admin 相关表结构
CREATE TABLE canal_config (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  cluster_id bigint(20) DEFAULT NULL,
  server_id bigint(20) DEFAULT NULL,
  name varchar(45) NOT NULL,
  content mediumtext,
  content_md5 varchar(128) NOT NULL,
  modified_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY sid_UNIQUE (server_id,name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE canal_adapter_config (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  category varchar(45) NOT NULL,
  name varchar(45) NOT NULL,
  status varchar(45) DEFAULT NULL,
  content mediumtext NOT NULL,
  modified_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE canal_cluster (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  name varchar(63) NOT NULL,
  zk_hosts varchar(255) NOT NULL,
  modified_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE canal_config_history (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  cluster_id bigint(20) DEFAULT NULL,
  server_id bigint(20) DEFAULT NULL,
  name varchar(45) DEFAULT NULL,
  content mediumtext,
  content_md5 varchar(128) DEFAULT NULL,
  modified_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE canal_instance_config (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  cluster_id bigint(20) DEFAULT NULL,
  server_id bigint(20) DEFAULT NULL,
  name varchar(45) NOT NULL,
  content mediumtext,
  content_md5 varchar(128) DEFAULT NULL,
  modified_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY sid_UNIQUE (server_id,name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE canal_node_server (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  cluster_id bigint(20) DEFAULT NULL,
  name varchar(63) NOT NULL,
  ip varchar(63) NOT NULL,
  admin_port int(11) DEFAULT NULL,
  tcp_port int(11) DEFAULT NULL,
  metric_port int(11) DEFAULT NULL,
  status varchar(45) DEFAULT NULL,
  modified_time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE canal_user (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  username varchar(31) NOT NULL,
  password varchar(128) NOT NULL,
  name varchar(31) NOT NULL,
  roles varchar(31) NOT NULL,
  introduction varchar(255) DEFAULT NULL,
  avatar varchar(255) DEFAULT NULL,
  creation_date timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 插入默认管理员用户 (用户名: admin, 密码: admin)
INSERT INTO canal_user (username, password, name, roles, introduction) VALUES 
('admin', '6BB4837EB74329105EE4568DDA7DC67ED2CA2AD06C327E92DA63CCB8DCC7CD80', 'Canal Admin', 'admin', 'Canal管理员');

-- 启用MySQL binlog（需要在my.cnf中配置，这里只是提醒）
-- 在 /etc/mysql/conf.d/my.cnf 中需要添加以下配置：
-- [mysqld]
-- log-bin=mysql-bin
-- binlog-format=ROW
-- server-id=1
-- expire_logs_days=7
-- binlog_cache_size=1M
-- max_binlog_cache_size=2G
-- max_binlog_size=1G

-- 给Canal用户授权（注意：生产环境应该创建专门的Canal用户）
-- GRANT SELECT, REPLICATION SLAVE, REPLICATION CLIENT ON *.* TO 'canal'@'%' IDENTIFIED BY 'canal';
-- FLUSH PRIVILEGES;

-- 查看binlog状态
SHOW MASTER STATUS; 