-- 添加缺少的文章权限
-- 确保所有按钮都有对应的权限控制

SET NAMES utf8mb4;

-- 开始事务
START TRANSACTION;

-- 1. 添加缺少的文章相关权限
-- 获取article:view权限ID作为父级权限
SET @article_view_id = (SELECT id FROM sys_permission WHERE permission_code = 'article:view');

-- 添加文章发布相关权限
INSERT IGNORE INTO `sys_permission` (`permission_code`, `permission_name`, `permission_type`, `parent_id`, `menu_path`, `component_path`, `icon`, `sort_order`, `status`, `description`, `create_time`, `update_time`) VALUES
('article:publish', '发布/下架文章', 'button', @article_view_id, NULL, NULL, NULL, 45, 1, '文章发布和下架权限', NOW(), NOW()),
('article:top', '置顶文章', 'button', @article_view_id, NULL, NULL, NULL, 46, 1, '设置文章置顶权限', NOW(), NOW()),
('article:recommend', '推荐文章', 'button', @article_view_id, NULL, NULL, NULL, 47, 1, '设置文章推荐权限', NOW(), NOW());

-- 2. 为超级管理员（role_id=1）分配所有权限
-- 获取新添加的权限ID
SET @article_publish_id = (SELECT id FROM sys_permission WHERE permission_code = 'article:publish');
SET @article_top_id = (SELECT id FROM sys_permission WHERE permission_code = 'article:top');
SET @article_recommend_id = (SELECT id FROM sys_permission WHERE permission_code = 'article:recommend');

-- 为超级管理员分配新权限
INSERT IGNORE INTO sys_role_permission (role_id, permission_id, create_time) VALUES
(1, @article_publish_id, NOW()),
(1, @article_top_id, NOW()),
(1, @article_recommend_id, NOW());

-- 3. 验证权限添加结果
SELECT '=== 新增的文章权限 ===' as info;
SELECT 
    p.permission_code,
    p.permission_name,
    p.permission_type,
    p.description
FROM sys_permission p
WHERE p.permission_code IN ('article:publish', 'article:top', 'article:recommend')
ORDER BY p.permission_code;

SELECT '=== 超级管理员的文章权限 ===' as info;
SELECT 
    r.role_name,
    p.permission_code,
    p.permission_name,
    p.permission_type
FROM sys_role r
JOIN sys_role_permission rp ON r.id = rp.role_id
JOIN sys_permission p ON rp.permission_id = p.id
WHERE r.id = 1 AND p.permission_code LIKE 'article:%'
ORDER BY p.permission_code;

SELECT '=== 内容管理员的文章权限 ===' as info;
SELECT 
    r.role_name,
    p.permission_code,
    p.permission_name,
    p.permission_type
FROM sys_role r
JOIN sys_role_permission rp ON r.id = rp.role_id
JOIN sys_permission p ON rp.permission_id = p.id
WHERE r.id = 2 AND p.permission_code LIKE 'article:%'
ORDER BY p.permission_code;

-- 提交事务
COMMIT; 