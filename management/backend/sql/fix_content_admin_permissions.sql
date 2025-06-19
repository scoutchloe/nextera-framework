-- 修复内容管理员权限的SQL脚本
-- 确保contentAdmin用户只能看到文章管理菜单，没有删除和发布权限

SET NAMES utf8mb4;

-- 1. 清理内容管理员角色的所有权限
DELETE FROM sys_role_permission WHERE role_id = 2;

-- 2. 为内容管理员角色（role_id=2）重新分配有限的权限

-- 仪表盘权限
INSERT INTO sys_role_permission (role_id, permission_id, create_time) 
SELECT 2, id, NOW() FROM sys_permission WHERE permission_code = 'dashboard:view';

-- 文章管理权限（使用article:*编码）
-- 文章管理父级权限
INSERT INTO sys_role_permission (role_id, permission_id, create_time) 
SELECT 2, id, NOW() FROM sys_permission WHERE permission_code = 'article:view';

-- 文章列表相关权限（仅查看、新增、编辑，不包括删除和发布）
INSERT INTO sys_role_permission (role_id, permission_id, create_time) 
SELECT 2, id, NOW() FROM sys_permission WHERE permission_code = 'article:list';

INSERT INTO sys_role_permission (role_id, permission_id, create_time) 
SELECT 2, id, NOW() FROM sys_permission WHERE permission_code = 'article:add';

INSERT INTO sys_role_permission (role_id, permission_id, create_time) 
SELECT 2, id, NOW() FROM sys_permission WHERE permission_code = 'article:edit';

-- 注意：不再分配 article:delete 和 article:publish 权限

-- 文章分类相关权限
INSERT INTO sys_role_permission (role_id, permission_id, create_time) 
SELECT 2, id, NOW() FROM sys_permission WHERE permission_code = 'article:category:list';

INSERT INTO sys_role_permission (role_id, permission_id, create_time) 
SELECT 2, id, NOW() FROM sys_permission WHERE permission_code = 'article:category:add';

INSERT INTO sys_role_permission (role_id, permission_id, create_time) 
SELECT 2, id, NOW() FROM sys_permission WHERE permission_code = 'article:category:edit';

-- 注意：不再分配 article:category:delete 权限

-- 文章标签相关权限
INSERT INTO sys_role_permission (role_id, permission_id, create_time) 
SELECT 2, id, NOW() FROM sys_permission WHERE permission_code = 'article:tag:list';

INSERT INTO sys_role_permission (role_id, permission_id, create_time) 
SELECT 2, id, NOW() FROM sys_permission WHERE permission_code = 'article:tag:add';

INSERT INTO sys_role_permission (role_id, permission_id, create_time) 
SELECT 2, id, NOW() FROM sys_permission WHERE permission_code = 'article:tag:edit';

-- 注意：不再分配 article:tag:delete 权限

-- 3. 验证contentAdmin用户的角色关联
SELECT 
    a.username,
    r.role_name,
    r.role_code
FROM admin a
JOIN admin_role ar ON a.id = ar.admin_id
JOIN sys_role r ON ar.role_id = r.id
WHERE a.username = 'contentAdmin';

-- 4. 验证权限分配结果
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

COMMIT; 