-- RocketMQ 事务消息表
-- 用于RocketMQ事务消息的可靠性保证和重试机制

CREATE TABLE IF NOT EXISTS `rocketmq_transaction_log` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `transaction_id` varchar(128) NOT NULL COMMENT '事务ID，业务唯一标识',
    `message_key` varchar(128) NOT NULL COMMENT '消息Key',
    `topic` varchar(64) NOT NULL COMMENT 'RocketMQ Topic',
    `tag` varchar(64) DEFAULT NULL COMMENT 'RocketMQ Tag',
    `message_body` text NOT NULL COMMENT '消息体内容',
    `message_status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '消息状态：0-准备中，1-已提交，2-已回滚',
    `retry_count` int(11) NOT NULL DEFAULT 0 COMMENT '重试次数',
    `max_retry_count` int(11) NOT NULL DEFAULT 3 COMMENT '最大重试次数',
    `business_type` varchar(32) NOT NULL COMMENT '业务类型：UPDATE_ARTICLE等',
    `business_id` varchar(64) NOT NULL COMMENT '业务ID',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `article_id` bigint(20) DEFAULT NULL COMMENT '文章ID',
    `local_transaction_status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '本地事务状态：0-准备中，1-已提交，2-已回滚',
    `error_message` text DEFAULT NULL COMMENT '错误信息',
    `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_by` varchar(64) DEFAULT 'system' COMMENT '创建人',
    `updated_by` varchar(64) DEFAULT 'system' COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_transaction_id` (`transaction_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_business_type` (`business_type`),
    KEY `idx_message_status` (`message_status`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RocketMQ事务消息日志表';

-- 用户操作记录表（用于记录用户更新文章的操作历史）
CREATE TABLE IF NOT EXISTS `user_article_operation_log` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `article_id` bigint(20) NOT NULL COMMENT '文章ID',
    `operation_type` varchar(32) NOT NULL COMMENT '操作类型：CREATE, UPDATE, DELETE',
    `operation_status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '操作状态：0-进行中，1-成功，2-失败',
    `transaction_id` varchar(128) NOT NULL COMMENT '关联的事务ID',
    `old_data` json DEFAULT NULL COMMENT '操作前的数据（JSON格式）',
    `new_data` json DEFAULT NULL COMMENT '操作后的数据（JSON格式）',
    `error_message` text DEFAULT NULL COMMENT '错误信息',
    `client_ip` varchar(64) DEFAULT NULL COMMENT '客户端IP',
    `user_agent` varchar(512) DEFAULT NULL COMMENT '用户代理',
    `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_transaction_id` (`transaction_id`),
    KEY `idx_operation_type` (`operation_type`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户文章操作日志表';

-- 补偿任务表（用于处理失败的事务）
CREATE TABLE IF NOT EXISTS `transaction_compensation_task` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `transaction_id` varchar(128) NOT NULL COMMENT '事务ID',
    `task_type` varchar(32) NOT NULL COMMENT '任务类型：ROLLBACK, RETRY',
    `task_status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '任务状态：0-待处理，1-处理中，2-已完成，3-已失败',
    `business_type` varchar(32) NOT NULL COMMENT '业务类型',
    `business_data` json NOT NULL COMMENT '业务数据（JSON格式）',
    `retry_count` int(11) NOT NULL DEFAULT 0 COMMENT '重试次数',
    `max_retry_count` int(11) NOT NULL DEFAULT 5 COMMENT '最大重试次数',
    `next_retry_time` datetime DEFAULT NULL COMMENT '下次重试时间',
    `error_message` text DEFAULT NULL COMMENT '错误信息',
    `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_transaction_id` (`transaction_id`),
    KEY `idx_task_status` (`task_status`),
    KEY `idx_next_retry_time` (`next_retry_time`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='事务补偿任务表'; 