-- 添加文章管理相关权限的SQL脚本
-- 为管理端添加article:*相关权限

SET NAMES utf8mb4;

-- 1. 添加文章管理权限
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `permission_type`, `parent_id`, `menu_path`, `component_path`, `icon`, `sort_order`, `status`, `description`, `create_time`, `update_time`) VALUES
-- 文章管理父级权限
('article:view', '文章管理', 'menu', 0, '/article', NULL, 'Document', 30, 1, '文章管理模块', NOW(), NOW()),

-- 文章列表权限
('article:list', '文章列表查看', 'menu', (SELECT id FROM sys_permission WHERE permission_code = 'article:view'), '/article/list', NULL, 'List', 31, 1, '文章列表页面', NOW(), NOW()),
('article:add', '新增文章', 'button', (SELECT id FROM sys_permission WHERE permission_code = 'article:list'), NULL, NULL, NULL, 32, 1, '新增文章权限', NOW(), NOW()),
('article:edit', '编辑文章', 'button', (SELECT id FROM sys_permission WHERE permission_code = 'article:list'), NULL, NULL, NULL, 33, 1, '编辑文章权限', NOW(), NOW()),
('article:delete', '删除文章', 'button', (SELECT id FROM sys_permission WHERE permission_code = 'article:list'), NULL, NULL, NULL, 34, 1, '删除文章权限', NOW(), NOW()),

-- 文章分类权限
('article:category:list', '分类管理查看', 'menu', (SELECT id FROM sys_permission WHERE permission_code = 'article:view'), '/article/category', NULL, 'Folder', 35, 1, '文章分类页面', NOW(), NOW()),
('article:category:add', '新增分类', 'button', (SELECT id FROM sys_permission WHERE permission_code = 'article:category:list'), NULL, NULL, NULL, 36, 1, '新增分类权限', NOW(), NOW()),
('article:category:edit', '编辑分类', 'button', (SELECT id FROM sys_permission WHERE permission_code = 'article:category:list'), NULL, NULL, NULL, 37, 1, '编辑分类权限', NOW(), NOW()),
('article:category:delete', '删除分类', 'button', (SELECT id FROM sys_permission WHERE permission_code = 'article:category:list'), NULL, NULL, NULL, 38, 1, '删除分类权限', NOW(), NOW()),

-- 文章标签权限
('article:tag:list', '标签管理查看', 'menu', (SELECT id FROM sys_permission WHERE permission_code = 'article:view'), '/article/tag', NULL, 'PriceTag', 39, 1, '文章标签页面', NOW(), NOW()),
('article:tag:add', '新增标签', 'button', (SELECT id FROM sys_permission WHERE permission_code = 'article:tag:list'), NULL, NULL, NULL, 40, 1, '新增标签权限', NOW(), NOW()),
('article:tag:edit', '编辑标签', 'button', (SELECT id FROM sys_permission WHERE permission_code = 'article:tag:list'), NULL, NULL, NULL, 41, 1, '编辑标签权限', NOW(), NOW()),
('article:tag:delete', '删除标签', 'button', (SELECT id FROM sys_permission WHERE permission_code = 'article:tag:list'), NULL, NULL, NULL, 42, 1, '删除标签权限', NOW(), NOW());

-- 2. 为内容管理员角色分配文章权限
-- 首先获取权限ID
SET @article_view_id = (SELECT id FROM sys_permission WHERE permission_code = 'article:view');
SET @article_list_id = (SELECT id FROM sys_permission WHERE permission_code = 'article:list');
SET @article_add_id = (SELECT id FROM sys_permission WHERE permission_code = 'article:add');
SET @article_edit_id = (SELECT id FROM sys_permission WHERE permission_code = 'article:edit');
SET @article_delete_id = (SELECT id FROM sys_permission WHERE permission_code = 'article:delete');

SET @article_category_list_id = (SELECT id FROM sys_permission WHERE permission_code = 'article:category:list');
SET @article_category_add_id = (SELECT id FROM sys_permission WHERE permission_code = 'article:category:add');
SET @article_category_edit_id = (SELECT id FROM sys_permission WHERE permission_code = 'article:category:edit');
SET @article_category_delete_id = (SELECT id FROM sys_permission WHERE permission_code = 'article:category:delete');

SET @article_tag_list_id = (SELECT id FROM sys_permission WHERE permission_code = 'article:tag:list');
SET @article_tag_add_id = (SELECT id FROM sys_permission WHERE permission_code = 'article:tag:add');
SET @article_tag_edit_id = (SELECT id FROM sys_permission WHERE permission_code = 'article:tag:edit');
SET @article_tag_delete_id = (SELECT id FROM sys_permission WHERE permission_code = 'article:tag:delete');

-- 为内容管理员角色（role_id=2）分配权限
INSERT INTO sys_role_permission (role_id, permission_id, create_time) VALUES
(2, @article_view_id, NOW()),
(2, @article_list_id, NOW()),
(2, @article_add_id, NOW()),
(2, @article_edit_id, NOW()),
(2, @article_delete_id, NOW()),
(2, @article_category_list_id, NOW()),
(2, @article_category_add_id, NOW()),
(2, @article_category_edit_id, NOW()),
(2, @article_category_delete_id, NOW()),
(2, @article_tag_list_id, NOW()),
(2, @article_tag_add_id, NOW()),
(2, @article_tag_edit_id, NOW()),
(2, @article_tag_delete_id, NOW());

-- 3. 验证权限分配
SELECT 
    r.role_name,
    p.permission_code,
    p.permission_name,
    p.permission_type
FROM sys_role r
JOIN sys_role_permission rp ON r.id = rp.role_id
JOIN sys_permission p ON rp.permission_id = p.id
WHERE r.id = 2 AND p.permission_code LIKE 'article:%'
ORDER BY p.sort_order; 