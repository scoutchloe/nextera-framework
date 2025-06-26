-- 为admin角色(角色ID=2)添加订单管理权限
-- 执行日期: 2025-06-26

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 添加订单管理相关权限到admin角色
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`) VALUES
(2, 30, NOW()), -- order:view 订单管理模块
(2, 31, NOW()), -- order:list:view 订单列表页面
(2, 32, NOW()), -- order:create 新增订单权限
(2, 33, NOW()), -- order:edit 编辑订单权限
(2, 34, NOW()); -- order:delete 删除订单权限

-- 验证添加结果
SELECT 
    r.role_name,
    p.permission_code,
    p.permission_name,
    rp.create_time
FROM sys_role_permission rp
JOIN sys_role r ON rp.role_id = r.id
JOIN sys_permission p ON rp.permission_id = p.id
WHERE r.id = 2 AND p.permission_code LIKE 'order:%'
ORDER BY p.permission_code;

SET FOREIGN_KEY_CHECKS = 1; 