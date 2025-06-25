-- =============================================
-- Nextera Order Module Database Initialization Script
-- =============================================

-- 创建4个订单数据库
CREATE DATABASE IF NOT EXISTS order_db_0 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS order_db_1 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS order_db_2 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS order_db_3 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- =============================================
-- 数据库 order_db_0 表结构
-- =============================================
USE order_db_0;

-- 订单表0
CREATE TABLE t_order_0 (
    id BIGINT PRIMARY KEY COMMENT '订单ID',
    order_no VARCHAR(64) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '订单状态(1:待支付,2:已支付,3:已发货,4:已完成,5:已取消)',
    payment_method TINYINT COMMENT '支付方式(1:微信,2:支付宝,3:银行卡)',
    remark VARCHAR(255) COMMENT '订单备注',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_order_no (order_no),
    INDEX idx_status (status),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表0';

-- 订单表1
CREATE TABLE t_order_1 (
    id BIGINT PRIMARY KEY COMMENT '订单ID',
    order_no VARCHAR(64) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '订单状态(1:待支付,2:已支付,3:已发货,4:已完成,5:已取消)',
    payment_method TINYINT COMMENT '支付方式(1:微信,2:支付宝,3:银行卡)',
    remark VARCHAR(255) COMMENT '订单备注',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_order_no (order_no),
    INDEX idx_status (status),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表1';

-- 订单表2
CREATE TABLE t_order_2 (
    id BIGINT PRIMARY KEY COMMENT '订单ID',
    order_no VARCHAR(64) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '订单状态(1:待支付,2:已支付,3:已发货,4:已完成,5:已取消)',
    payment_method TINYINT COMMENT '支付方式(1:微信,2:支付宝,3:银行卡)',
    remark VARCHAR(255) COMMENT '订单备注',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_order_no (order_no),
    INDEX idx_status (status),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表2';

-- 订单表3
CREATE TABLE t_order_3 (
    id BIGINT PRIMARY KEY COMMENT '订单ID',
    order_no VARCHAR(64) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '订单状态(1:待支付,2:已支付,3:已发货,4:已完成,5:已取消)',
    payment_method TINYINT COMMENT '支付方式(1:微信,2:支付宝,3:银行卡)',
    remark VARCHAR(255) COMMENT '订单备注',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX idx_user_id (user_id),
    INDEX idx_order_no (order_no),
    INDEX idx_status (status),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表3';

-- 订单明细表0
CREATE TABLE t_order_item_0 (
    id BIGINT PRIMARY KEY COMMENT '明细ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    user_id BIGINT NOT NULL COMMENT '用户ID（分片键）',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(255) NOT NULL COMMENT '商品名称',
    product_price DECIMAL(10,2) NOT NULL COMMENT '商品单价',
    quantity INT NOT NULL COMMENT '购买数量',
    total_price DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_order_id (order_id),
    INDEX idx_user_id (user_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表0';

-- 订单明细表1
CREATE TABLE t_order_item_1 (
    id BIGINT PRIMARY KEY COMMENT '明细ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    user_id BIGINT NOT NULL COMMENT '用户ID（分片键）',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(255) NOT NULL COMMENT '商品名称',
    product_price DECIMAL(10,2) NOT NULL COMMENT '商品单价',
    quantity INT NOT NULL COMMENT '购买数量',
    total_price DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_order_id (order_id),
    INDEX idx_user_id (user_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表1';

-- 订单明细表2
CREATE TABLE t_order_item_2 (
    id BIGINT PRIMARY KEY COMMENT '明细ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    user_id BIGINT NOT NULL COMMENT '用户ID（分片键）',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(255) NOT NULL COMMENT '商品名称',
    product_price DECIMAL(10,2) NOT NULL COMMENT '商品单价',
    quantity INT NOT NULL COMMENT '购买数量',
    total_price DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_order_id (order_id),
    INDEX idx_user_id (user_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表2';

-- 订单明细表3
CREATE TABLE t_order_item_3 (
    id BIGINT PRIMARY KEY COMMENT '明细ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    user_id BIGINT NOT NULL COMMENT '用户ID（分片键）',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(255) NOT NULL COMMENT '商品名称',
    product_price DECIMAL(10,2) NOT NULL COMMENT '商品单价',
    quantity INT NOT NULL COMMENT '购买数量',
    total_price DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    
    INDEX idx_order_id (order_id),
    INDEX idx_user_id (user_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表3';

-- =============================================
-- 复制相同结构到其他3个数据库
-- =============================================

-- 数据库 order_db_1
USE order_db_1;

CREATE TABLE t_order_0 LIKE order_db_0.t_order_0;
CREATE TABLE t_order_1 LIKE order_db_0.t_order_1;
CREATE TABLE t_order_2 LIKE order_db_0.t_order_2;
CREATE TABLE t_order_3 LIKE order_db_0.t_order_3;

CREATE TABLE t_order_item_0 LIKE order_db_0.t_order_item_0;
CREATE TABLE t_order_item_1 LIKE order_db_0.t_order_item_1;
CREATE TABLE t_order_item_2 LIKE order_db_0.t_order_item_2;
CREATE TABLE t_order_item_3 LIKE order_db_0.t_order_item_3;

-- 数据库 order_db_2
USE order_db_2;

CREATE TABLE t_order_0 LIKE order_db_0.t_order_0;
CREATE TABLE t_order_1 LIKE order_db_0.t_order_1;
CREATE TABLE t_order_2 LIKE order_db_0.t_order_2;
CREATE TABLE t_order_3 LIKE order_db_0.t_order_3;

CREATE TABLE t_order_item_0 LIKE order_db_0.t_order_item_0;
CREATE TABLE t_order_item_1 LIKE order_db_0.t_order_item_1;
CREATE TABLE t_order_item_2 LIKE order_db_0.t_order_item_2;
CREATE TABLE t_order_item_3 LIKE order_db_0.t_order_item_3;

-- 数据库 order_db_3
USE order_db_3;

CREATE TABLE t_order_0 LIKE order_db_0.t_order_0;
CREATE TABLE t_order_1 LIKE order_db_0.t_order_1;
CREATE TABLE t_order_2 LIKE order_db_0.t_order_2;
CREATE TABLE t_order_3 LIKE order_db_0.t_order_3;

CREATE TABLE t_order_item_0 LIKE order_db_0.t_order_item_0;
CREATE TABLE t_order_item_1 LIKE order_db_0.t_order_item_1;
CREATE TABLE t_order_item_2 LIKE order_db_0.t_order_item_2;
CREATE TABLE t_order_item_3 LIKE order_db_0.t_order_item_3;

-- =============================================
-- 数据库初始化完成
-- ============================================= 