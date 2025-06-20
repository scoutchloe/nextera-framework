-- 完整的文章权限修复脚本
-- 1. 添加article:*权限到系统
-- 2. 为内容管理员分配正确的权限

SET NAMES utf8mb4;

-- 开始事务
START TRANSACTION;

-- 1. 删除可能存在的旧content相关权限分配（仅针对内容管理员角色）
DELETE FROM sys_role_permission WHERE role_id = 2;

-- 2. 添加文章管理权限（如果不存在）
-- 检查并添加父级权限
INSERT IGNORE INTO `sys_permission` (`permission_code`, `permission_name`, `permission_type`, `parent_id`, `menu_path`, `component_path`, `icon`, `sort_order`, `status`, `description`, `create_time`, `update_time`) VALUES
('article:view', '文章管理', 'menu', 0, '/article', NULL, 'Document', 30, 1, '文章管理模块', NOW(), NOW());

-- 获取article:view权限ID
SET @article_view_id = (SELECT id FROM sys_permission WHERE permission_code = 'article:view');

-- 添加子级权限
INSERT IGNORE INTO `sys_permission` (`permission_code`, `permission_name`, `permission_type`, `parent_id`, `menu_path`, `component_path`, `icon`, `sort_order`, `status`, `description`, `create_time`, `update_time`) VALUES
-- 文章列表权限
('article:list', '文章列表查看', 'menu', @article_view_id, '/article/list', NULL, 'List', 31, 1, '文章列表页面', NOW(), NOW()),
('article:add', '新增文章', 'button', @article_view_id, NULL, NULL, NULL, 32, 1, '新增文章权限', NOW(), NOW()),
('article:edit', '编辑文章', 'button', @article_view_id, NULL, NULL, NULL, 33, 1, '编辑文章权限', NOW(), NOW()),
('article:delete', '删除文章', 'button', @article_view_id, NULL, NULL, NULL, 34, 1, '删除文章权限', NOW(), NOW()),

-- 文章分类权限
('article:category:list', '分类管理查看', 'menu', @article_view_id, '/article/category', NULL, 'Folder', 35, 1, '文章分类页面', NOW(), NOW()),
('article:category:add', '新增分类', 'button', @article_view_id, NULL, NULL, NULL, 36, 1, '新增分类权限', NOW(), NOW()),
('article:category:edit', '编辑分类', 'button', @article_view_id, NULL, NULL, NULL, 37, 1, '编辑分类权限', NOW(), NOW()),
('article:category:delete', '删除分类', 'button', @article_view_id, NULL, NULL, NULL, 38, 1, '删除分类权限', NOW(), NOW()),

-- 文章标签权限
('article:tag:list', '标签管理查看', 'menu', @article_view_id, '/article/tag', NULL, 'PriceTag', 39, 1, '文章标签页面', NOW(), NOW()),
('article:tag:add', '新增标签', 'button', @article_view_id, NULL, NULL, NULL, 40, 1, '新增标签权限', NOW(), NOW()),
('article:tag:edit', '编辑标签', 'button', @article_view_id, NULL, NULL, NULL, 41, 1, '编辑标签权限', NOW(), NOW()),
('article:tag:delete', '删除标签', 'button', @article_view_id, NULL, NULL, NULL, 42, 1, '删除标签权限', NOW(), NOW());

-- 3. 为内容管理员角色分配权限
-- 仪表盘权限
INSERT IGNORE INTO sys_role_permission (role_id, permission_id, create_time) 
SELECT 2, id, NOW() FROM sys_permission WHERE permission_code = 'dashboard:view';

-- 文章管理权限
INSERT IGNORE INTO sys_role_permission (role_id, permission_id, create_time) 
SELECT 2, id, NOW() FROM sys_permission WHERE permission_code IN (
    'article:view',
    'article:list', 
    'article:add', 
    'article:edit', 
    'article:delete',
    'article:category:list', 
    'article:category:add', 
    'article:category:edit', 
    'article:category:delete',
    'article:tag:list', 
    'article:tag:add', 
    'article:tag:edit', 
    'article:tag:delete'
);

-- 4. 验证contentAdmin用户的角色关联
-- 确保contentAdmin用户存在并关联到内容管理员角色
INSERT IGNORE INTO admin_role (admin_id, role_id, create_time) 
SELECT a.id, 2, NOW() 
FROM admin a 
WHERE a.username = 'contentAdmin' 
AND NOT EXISTS (
    SELECT 1 FROM admin_role ar WHERE ar.admin_id = a.id AND ar.role_id = 2
);

-- 5. 验证权限分配结果
SELECT '=== ContentAdmin用户角色关联 ===' as info;
SELECT 
    a.username,
    r.role_name,
    r.role_code
FROM admin a
JOIN admin_role ar ON a.id = ar.admin_id
JOIN sys_role r ON ar.role_id = r.id
WHERE a.username = 'contentAdmin';

SELECT '=== 内容管理员角色权限列表 ===' as info;
SELECT 
    r.role_name,
    p.permission_code,
    p.permission_name,
    p.permission_type
FROM sys_role r
JOIN sys_role_permission rp ON r.id = rp.role_id
JOIN sys_permission p ON rp.permission_id = p.id
WHERE r.id = 2
ORDER BY p.permission_code;

-- 提交事务
COMMIT;

SELECT '=== 修复完成 ===' as result; 