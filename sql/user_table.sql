-- =============================================
-- 用户相关表建表语句
-- 根据 nextera-user/entity/User.java 和 UserProfile.java 生成
-- =============================================

DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
    `id` BIGINT NOT NULL COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `status` TINYINT DEFAULT 0 COMMENT '用户状态：0-正常，1-禁用',
    `gender` TINYINT DEFAULT 0 COMMENT '性别：0-未知，1-男，2-女',
    `birthday` DATETIME DEFAULT NULL COMMENT '生日',
    `bio` TEXT DEFAULT NULL COMMENT '个人简介',
    `website` VARCHAR(255) DEFAULT NULL COMMENT '个人网站',
    `location` VARCHAR(100) DEFAULT NULL COMMENT '所在地',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 初始化数据
-- =============================================

INSERT INTO `user` (
    `id`, 
    `username`, 
    `password`, 
    `nickname`, 
    `email`, 
    `phone`, 
    `avatar`, 
    `status`, 
    `gender`, 
    `birthday`, 
    `bio`, 
    `website`, 
    `location`, 
    `create_time`, 
    `update_time`, 
    `last_login_time`, 
    `is_deleted`
) VALUES 
(
    1, 
    'admin', 
    '$2a$10$7JB720yubVSOfvVWbGReyO.ZhMqcpAp132bNCyBjNR1.dJedHu97m', -- 密码: admin123
    '系统管理员', 
    'admin@nextera.com', 
    '13800138000', 
    'https://avatars.githubusercontent.com/u/1?v=4', 
    0, 
    1, 
    '1990-01-01 00:00:00', 
    '系统管理员账户', 
    'https://nextera.com', 
    '北京市', 
    NOW(), 
    NOW(), 
    NOW(), 
    0
),
(
    2, 
    'testuser', 
    '$2a$10$7JB720yubVSOfvVWbGReyO.ZhMqcpAp132bNCyBjNR1.dJedHu97m', -- 密码: admin123
    '测试用户', 
    'test@nextera.com', 
    '13800138001', 
    'https://avatars.githubusercontent.com/u/2?v=4', 
    0, 
    2, 
    '1995-05-15 00:00:00', 
    '这是一个测试用户账户', 
    'https://test.nextera.com', 
    '上海市', 
    NOW(), 
    NOW(), 
    NOW(), 
    0
),
(
    3, 
    'author', 
    '$2a$10$7JB720yubVSOfvVWbGReyO.ZhMqcpAp132bNCyBjNR1.dJedHu97m', -- 密码: admin123
    '文章作者', 
    'author@nextera.com', 
    '13800138002', 
    'https://avatars.githubusercontent.com/u/3?v=4', 
    0, 
    1, 
    '1988-12-20 00:00:00', 
    '专业的技术文章作者，专注于Java和微服务技术分享', 
    'https://author.nextera.com', 
    '深圳市', 
    NOW(), 
    NOW(), 
    NOW(), 
    0
);

-- =============================================
-- 表结构说明
-- =============================================

/*
表名: user
说明: 用户基础信息表

字段说明:
- id: 用户ID，使用MyBatis-Plus的ASSIGN_ID策略生成
- username: 用户名，唯一索引，用于登录
- password: 密码，BCrypt加密存储
- nickname: 昵称，用于显示
- email: 邮箱，唯一索引，可用于登录和找回密码
- phone: 手机号，唯一索引，可用于登录
- avatar: 头像URL，支持长URL
- status: 用户状态，0-正常，1-禁用
- gender: 性别，0-未知，1-男，2-女
- birthday: 生日，可为空
- bio: 个人简介，使用TEXT类型支持长文本
- website: 个人网站URL
- location: 所在地
- create_time: 创建时间，自动设置
- update_time: 更新时间，自动更新
- last_login_time: 最后登录时间
- is_deleted: 逻辑删除标志，0-未删除，1-已删除

索引说明:
- PRIMARY KEY: id主键
- UNIQUE KEY: username, email, phone唯一索引
- KEY: status, create_time, is_deleted普通索引

字符集: utf8mb4_unicode_ci，支持emoji和多语言
引擎: InnoDB，支持事务和外键
*/

-- =============================================
-- 用户资料扩展表建表语句
-- 根据 nextera-user/entity/UserProfile.java 生成
-- =============================================

DROP TABLE IF EXISTS `user_profile`;

CREATE TABLE `user_profile` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户资料ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `age` INT DEFAULT NULL COMMENT '年龄',
    `education` VARCHAR(50) DEFAULT NULL COMMENT '学历',
    `profession` VARCHAR(100) DEFAULT NULL COMMENT '职业',
    `location` VARCHAR(100) DEFAULT NULL COMMENT '所在地',
    `website` VARCHAR(255) DEFAULT NULL COMMENT '个人网站',
    `github` VARCHAR(255) DEFAULT NULL COMMENT 'GitHub地址',
    `wechat` VARCHAR(50) DEFAULT NULL COMMENT '微信号',
    `qq` VARCHAR(20) DEFAULT NULL COMMENT 'QQ号',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_user_profile_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户资料扩展表';

-- =============================================
-- 用户资料扩展表初始化数据
-- =============================================

INSERT INTO `user_profile` (
    `user_id`, 
    `real_name`, 
    `age`, 
    `education`, 
    `profession`, 
    `location`, 
    `website`, 
    `github`, 
    `wechat`, 
    `qq`, 
    `create_time`, 
    `update_time`
) VALUES 
(
    1, 
    '张管理员', 
    35, 
    '计算机科学与技术硕士', 
    '系统架构师', 
    '北京市海淀区', 
    'https://nextera.com', 
    'https://github.com/admin', 
    'nextera_admin', 
    '123456789', 
    NOW(), 
    NOW()
),
(
    2, 
    '李测试', 
    28, 
    '软件工程本科', 
    '软件测试工程师', 
    '上海市浦东新区', 
    'https://test.nextera.com', 
    'https://github.com/testuser', 
    'nextera_test', 
    '987654321', 
    NOW(), 
    NOW()
),
(
    3, 
    '王作者', 
    37, 
    '计算机应用技术博士', 
    '高级Java开发工程师', 
    '深圳市南山区', 
    'https://author.nextera.com', 
    'https://github.com/author', 
    'nextera_author', 
    '555666777', 
    NOW(), 
    NOW()
);

-- =============================================
-- 用户资料扩展表结构说明
-- =============================================

/*
表名: user_profile
说明: 用户资料扩展表，存储用户的详细资料信息

字段说明:
- id: 用户资料ID，自增主键
- user_id: 用户ID，关联user表的id字段
- real_name: 真实姓名
- age: 年龄
- education: 学历
- profession: 职业
- location: 所在地
- website: 个人网站
- github: GitHub地址
- wechat: 微信号
- qq: QQ号
- create_time: 创建时间，自动设置
- update_time: 更新时间，自动更新

索引说明:
- PRIMARY KEY: id主键
- UNIQUE KEY: user_id唯一索引，确保一个用户只有一条资料记录
- KEY: create_time普通索引
- FOREIGN KEY: user_id外键，关联user表

关系说明:
- 与user表是一对一关系
- 当user表记录删除时，对应的user_profile记录也会被删除（CASCADE）
- 当user表的id更新时，user_profile的user_id也会同步更新（CASCADE）

字符集: utf8mb4_unicode_ci，支持emoji和多语言
引擎: InnoDB，支持事务和外键
*/ 