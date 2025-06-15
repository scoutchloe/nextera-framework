-- Nextera Framework 数据库初始化脚本
-- 创建时间: 2024-01-01
-- 版本: 1.0.0

-- 创建数据库
CREATE DATABASE IF NOT EXISTS nextera_auth DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS nextera_user DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS nextera_article DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;

-- 切换到认证数据库
USE nextera_auth;

-- 创建用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) NOT NULL COMMENT '昵称',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    phone VARCHAR(20) UNIQUE COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像URL',
    gender CHAR(1) DEFAULT '0' COMMENT '性别(0-未知 1-男 2-女)',
    birthday DATETIME COMMENT '生日',
    bio TEXT COMMENT '个人简介',
    status CHAR(1) DEFAULT '0' COMMENT '状态(0-正常 1-停用 2-删除)',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(50) COMMENT '创建者',
    update_by VARCHAR(50) COMMENT '更新者',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志(0-存在 1-删除)',
    remark VARCHAR(500) COMMENT '备注',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_phone (phone),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) COMMENT='用户表';

-- 创建角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_key VARCHAR(50) NOT NULL UNIQUE COMMENT '角色权限字符串',
    role_sort INT DEFAULT 0 COMMENT '显示顺序',
    data_scope CHAR(1) DEFAULT '1' COMMENT '数据范围(1-全部数据权限 2-自定义数据权限 3-本部门数据权限 4-本部门及以下数据权限)',
    menu_check_strictly TINYINT DEFAULT 1 COMMENT '菜单树选择项是否关联显示',
    dept_check_strictly TINYINT DEFAULT 1 COMMENT '部门树选择项是否关联显示',
    status CHAR(1) DEFAULT '0' COMMENT '角色状态(0-正常 1-停用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(50) COMMENT '创建者',
    update_by VARCHAR(50) COMMENT '更新者',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志(0-存在 1-删除)',
    remark VARCHAR(500) COMMENT '备注',
    INDEX idx_role_key (role_key),
    INDEX idx_status (status)
) COMMENT='角色表';

-- 创建用户角色关联表
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE
) COMMENT='用户角色关联表';

-- 创建菜单表
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '菜单ID',
    menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID',
    order_num INT DEFAULT 0 COMMENT '显示顺序',
    path VARCHAR(200) COMMENT '路由地址',
    component VARCHAR(255) COMMENT '组件路径',
    query VARCHAR(255) COMMENT '路由参数',
    is_frame TINYINT DEFAULT 1 COMMENT '是否为外链(0-是 1-否)',
    is_cache TINYINT DEFAULT 0 COMMENT '是否缓存(0-缓存 1-不缓存)',
    menu_type CHAR(1) COMMENT '菜单类型(M-目录 C-菜单 F-按钮)',
    visible CHAR(1) DEFAULT '0' COMMENT '显示状态(0-显示 1-隐藏)',
    status CHAR(1) DEFAULT '0' COMMENT '菜单状态(0-正常 1-停用)',
    perms VARCHAR(100) COMMENT '权限标识',
    icon VARCHAR(100) COMMENT '菜单图标',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(50) COMMENT '创建者',
    update_by VARCHAR(50) COMMENT '更新者',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志(0-存在 1-删除)',
    remark VARCHAR(500) COMMENT '备注',
    INDEX idx_parent_id (parent_id),
    INDEX idx_menu_type (menu_type),
    INDEX idx_status (status)
) COMMENT='菜单权限表';

-- 创建角色菜单关联表
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    role_id BIGINT NOT NULL COMMENT '角色ID',
    menu_id BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (role_id, menu_id),
    FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
    FOREIGN KEY (menu_id) REFERENCES sys_menu(id) ON DELETE CASCADE
) COMMENT='角色菜单关联表';

-- 插入初始数据

-- 插入超级管理员角色
INSERT INTO sys_role (id, role_name, role_key, role_sort, data_scope, status, create_by, remark) VALUES 
(1, '超级管理员', 'admin', 1, '1', '0', 'system', '超级管理员角色'),
(2, '普通用户', 'user', 2, '5', '0', 'system', '普通用户角色');

-- 插入管理员用户 (密码: admin123)
INSERT INTO sys_user (id, username, password, nickname, email, status, create_by, remark) VALUES 
(1, 'admin', '$2a$10$7JB720yubVSOfvVOZGVkMe5w2x8KELEyqKCVOqFNlnSYxHCjGJjLm', '系统管理员', 'admin@nextera.com', '0', 'system', '系统管理员账户'),
(2, 'user', '$2a$10$7JB720yubVSOfvVOZGVkMe5w2x8KELEyqKCVOqFNlnSYxHCjGJjLm', '普通用户', 'user@nextera.com', '0', 'system', '普通用户账户');

-- 插入用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES 
(1, 1),
(2, 2);

-- 插入系统菜单
INSERT INTO sys_menu (id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, remark) VALUES
-- 系统管理
(1, '系统管理', 0, 1, 'system', '', 'M', '0', '0', '', 'system', 'system', '系统管理目录'),
(2, '用户管理', 1, 1, 'user', 'system/user/index', 'C', '0', '0', 'system:user:list', 'user', 'system', '用户管理菜单'),
(3, '角色管理', 1, 2, 'role', 'system/role/index', 'C', '0', '0', 'system:role:list', 'peoples', 'system', '角色管理菜单'),
(4, '菜单管理', 1, 3, 'menu', 'system/menu/index', 'C', '0', '0', 'system:menu:list', 'tree-table', 'system', '菜单管理菜单'),

-- 用户管理按钮
(100, '用户查询', 2, 1, '', '', 'F', '0', '0', 'system:user:query', '#', 'system', ''),
(101, '用户新增', 2, 2, '', '', 'F', '0', '0', 'system:user:add', '#', 'system', ''),
(102, '用户修改', 2, 3, '', '', 'F', '0', '0', 'system:user:edit', '#', 'system', ''),
(103, '用户删除', 2, 4, '', '', 'F', '0', '0', 'system:user:remove', '#', 'system', ''),
(104, '用户导出', 2, 5, '', '', 'F', '0', '0', 'system:user:export', '#', 'system', ''),
(105, '用户导入', 2, 6, '', '', 'F', '0', '0', 'system:user:import', '#', 'system', ''),
(106, '重置密码', 2, 7, '', '', 'F', '0', '0', 'system:user:resetPwd', '#', 'system', ''),

-- 角色管理按钮
(200, '角色查询', 3, 1, '', '', 'F', '0', '0', 'system:role:query', '#', 'system', ''),
(201, '角色新增', 3, 2, '', '', 'F', '0', '0', 'system:role:add', '#', 'system', ''),
(202, '角色修改', 3, 3, '', '', 'F', '0', '0', 'system:role:edit', '#', 'system', ''),
(203, '角色删除', 3, 4, '', '', 'F', '0', '0', 'system:role:remove', '#', 'system', ''),
(204, '角色导出', 3, 5, '', '', 'F', '0', '0', 'system:role:export', '#', 'system', ''),

-- 菜单管理按钮
(300, '菜单查询', 4, 1, '', '', 'F', '0', '0', 'system:menu:query', '#', 'system', ''),
(301, '菜单新增', 4, 2, '', '', 'F', '0', '0', 'system:menu:add', '#', 'system', ''),
(302, '菜单修改', 4, 3, '', '', 'F', '0', '0', 'system:menu:edit', '#', 'system', ''),
(303, '菜单删除', 4, 4, '', '', 'F', '0', '0', 'system:menu:remove', '#', 'system', ''),

-- 内容管理
(1000, '内容管理', 0, 2, 'content', '', 'M', '0', '0', '', 'documentation', 'system', '内容管理目录'),
(1001, '文章管理', 1000, 1, 'article', 'content/article/index', 'C', '0', '0', 'content:article:list', 'edit', 'system', '文章管理菜单'),
(1002, '分类管理', 1000, 2, 'category', 'content/category/index', 'C', '0', '0', 'content:category:list', 'tree', 'system', '分类管理菜单');

-- 插入角色菜单关联 (管理员拥有所有权限)
INSERT INTO sys_role_menu (role_id, menu_id) 
SELECT 1, id FROM sys_menu WHERE deleted = 0;

-- 插入普通用户权限 (只有基本查询权限)
INSERT INTO sys_role_menu (role_id, menu_id) VALUES 
(2, 1000), (2, 1001), (2, 1002);

-- 切换到用户数据库
USE nextera_user;

-- 创建用户资料表
DROP TABLE IF EXISTS user_profile;
CREATE TABLE user_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户资料ID',
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    real_name VARCHAR(50) COMMENT '真实姓名',
    age INT COMMENT '年龄',
    education VARCHAR(50) COMMENT '学历',
    profession VARCHAR(100) COMMENT '职业',
    location VARCHAR(100) COMMENT '所在地',
    website VARCHAR(255) COMMENT '个人网站',
    github VARCHAR(255) COMMENT 'GitHub地址',
    wechat VARCHAR(50) COMMENT '微信号',
    qq VARCHAR(20) COMMENT 'QQ号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id)
) COMMENT='用户资料扩展表';

-- 切换到文章数据库
USE nextera_article;

-- 创建文章分类表
DROP TABLE IF EXISTS article_category;
CREATE TABLE article_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    category_name VARCHAR(50) NOT NULL COMMENT '分类名称',
    category_key VARCHAR(50) NOT NULL UNIQUE COMMENT '分类标识',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    sort_order INT DEFAULT 0 COMMENT '排序',
    description TEXT COMMENT '分类描述',
    cover_image VARCHAR(255) COMMENT '封面图片',
    status CHAR(1) DEFAULT '0' COMMENT '状态(0-正常 1-停用)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(50) COMMENT '创建者',
    update_by VARCHAR(50) COMMENT '更新者',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志(0-存在 1-删除)',
    remark VARCHAR(500) COMMENT '备注',
    INDEX idx_category_key (category_key),
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status)
) COMMENT='文章分类表';

-- 创建文章表
DROP TABLE IF EXISTS article;
CREATE TABLE article (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文章ID',
    title VARCHAR(200) NOT NULL COMMENT '文章标题',
    content LONGTEXT COMMENT '文章内容',
    summary TEXT COMMENT '文章摘要',
    cover_image VARCHAR(255) COMMENT '封面图片',
    author_id BIGINT NOT NULL COMMENT '作者ID',
    author_name VARCHAR(50) COMMENT '作者名称',
    category_id BIGINT COMMENT '分类ID',
    tags VARCHAR(500) COMMENT '标签(用逗号分隔)',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    like_count INT DEFAULT 0 COMMENT '点赞次数',
    comment_count INT DEFAULT 0 COMMENT '评论次数',
    is_top TINYINT DEFAULT 0 COMMENT '是否置顶(0-否 1-是)',
    is_recommend TINYINT DEFAULT 0 COMMENT '是否推荐(0-否 1-是)',
    status CHAR(1) DEFAULT '0' COMMENT '状态(0-草稿 1-已发布 2-已下线)',
    publish_time DATETIME COMMENT '发布时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by VARCHAR(50) COMMENT '创建者',
    update_by VARCHAR(50) COMMENT '更新者',
    deleted TINYINT DEFAULT 0 COMMENT '删除标志(0-存在 1-删除)',
    remark VARCHAR(500) COMMENT '备注',
    INDEX idx_author_id (author_id),
    INDEX idx_category_id (category_id),
    INDEX idx_status (status),
    INDEX idx_publish_time (publish_time),
    INDEX idx_is_top (is_top),
    INDEX idx_is_recommend (is_recommend),
    FOREIGN KEY (category_id) REFERENCES article_category(id)
) COMMENT='文章表';

-- 插入初始分类数据
INSERT INTO article_category (id, category_name, category_key, parent_id, sort_order, description, status, create_by) VALUES
(1, '技术分享', 'tech', 0, 1, '技术相关文章', '0', 'system'),
(2, '生活随笔', 'life', 0, 2, '生活感悟文章', '0', 'system'),
(3, '学习笔记', 'study', 0, 3, '学习记录文章', '0', 'system'),
(4, 'Java', 'java', 1, 1, 'Java技术文章', '0', 'system'),
(5, 'Vue.js', 'vue', 1, 2, 'Vue.js技术文章', '0', 'system'),
(6, 'Spring Boot', 'spring-boot', 1, 3, 'Spring Boot技术文章', '0', 'system');

-- 插入示例文章
INSERT INTO article (id, title, content, summary, author_id, author_name, category_id, tags, status, publish_time, create_by) VALUES
(1, '欢迎使用Nextera框架', '# 欢迎使用Nextera框架\n\nNextera是一个基于Spring Cloud的现代化微服务开发框架...\n\n## 主要特性\n\n- 微服务架构\n- 统一认证\n- 权限管理\n- 前后端分离\n\n## 快速开始\n\n1. 克隆项目\n2. 启动基础服务\n3. 运行应用\n\n祝您使用愉快！', '这是Nextera框架的介绍文章，包含了框架的主要特性和使用方法。', 1, '系统管理员', 1, 'Nextera,框架,微服务', '1', NOW(), 'system');

COMMIT; 