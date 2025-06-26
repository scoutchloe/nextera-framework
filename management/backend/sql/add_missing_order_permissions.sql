SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 添加缺失的订单权限
INSERT INTO `sys_permission` (`permission_code`, `permission_name`, `permission_type`, `parent_id`, `sort_order`, `status`, `description`, `create_time`) VALUES
('order:export', '导出订单', 'button', 31, 35, 1, '导出订单数据权限', NOW()),
('order:sync', '同步订单', 'button', 31, 36, 1, '同步订单到ES权限', NOW());

-- 为admin角色(角色ID=2)添加这些权限
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`, `create_time`) 
SELECT 2, id, NOW() FROM `sys_permission` WHERE `permission_code` IN ('order:export', 'order:sync');

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