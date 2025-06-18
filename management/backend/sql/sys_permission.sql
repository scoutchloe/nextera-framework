/*
 Navicat Premium Data Transfer

 Source Server         : docker-mysql-5.7
 Source Server Type    : MySQL
 Source Server Version : 50736
 Source Host           : localhost:3306
 Source Schema         : managementlightHouse

 Target Server Type    : MySQL
 Target Server Version : 50736
 File Encoding         : 65001

 Date: 18/06/2025 15:38:25
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `permission_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限编码',
  `permission_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限名称',
  `permission_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'menu' COMMENT '权限类型：menu-菜单，button-按钮',
  `parent_id` bigint(20) NOT NULL DEFAULT 0 COMMENT '父级权限ID，0表示顶级',
  `menu_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜单路径',
  `component_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '组件路径',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `sort_order` int(11) NOT NULL DEFAULT 0 COMMENT '排序序号',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权限描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint(20) NULL DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_permission_code`(`permission_code`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 65 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_permission
-- ----------------------------
INSERT INTO `sys_permission` VALUES (1, 'dashboard:view', '仪表盘', 'menu', 0, '/dashboard', NULL, NULL, 1, 1, '仪表盘查看权限', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (10, 'content:view', '内容管理', 'menu', 0, '/content', NULL, NULL, 10, 1, '内容管理模块', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (11, 'content:space:view', '空间管理', 'menu', 10, '/content/spaces', NULL, NULL, 11, 1, '空间管理页面', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (12, 'content:space:create', '新增空间', 'button', 11, NULL, NULL, NULL, 12, 1, '新增空间权限', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (13, 'content:space:edit', '编辑空间', 'button', 11, NULL, NULL, NULL, 13, 1, '编辑空间权限', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (14, 'content:space:delete', '删除空间', 'button', 11, NULL, NULL, NULL, 14, 1, '删除空间权限', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (15, 'content:problem:view', '问题管理', 'menu', 10, '/content/problems', NULL, NULL, 15, 1, '问题管理页面', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (16, 'content:problem:create', '新增问题', 'button', 15, NULL, NULL, NULL, 16, 1, '新增问题权限', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (17, 'content:problem:edit', '编辑问题', 'button', 15, NULL, NULL, NULL, 17, 1, '编辑问题权限', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (18, 'content:problem:delete', '删除问题', 'button', 15, NULL, NULL, NULL, 18, 1, '删除问题权限', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (19, 'content:solution:view', '解决方案', 'menu', 10, '/content/solutions', NULL, NULL, 19, 1, '解决方案页面', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (20, 'content:solution:create', '新增方案', 'button', 19, NULL, NULL, NULL, 20, 1, '新增方案权限', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (21, 'content:solution:edit', '编辑方案', 'button', 19, NULL, NULL, NULL, 21, 1, '编辑方案权限', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (22, 'content:solution:delete', '删除方案', 'button', 19, NULL, NULL, NULL, 22, 1, '删除方案权限', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (23, 'content:banner:view', '轮播图管理', 'menu', 10, '/content/banners', NULL, NULL, 23, 1, '轮播图管理页面', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (24, 'content:recommendation:view', '推荐内容', 'menu', 10, '/content/recommendations', NULL, NULL, 24, 1, '推荐内容页面', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (30, 'order:view', '订单管理', 'menu', 0, '/order', NULL, NULL, 30, 1, '订单管理模块', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (31, 'order:list:view', '订单列表', 'menu', 30, '/order/list', NULL, NULL, 31, 1, '订单列表页面', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (32, 'order:create', '新增订单', 'button', 31, NULL, NULL, NULL, 32, 1, '新增订单权限', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (33, 'order:edit', '编辑订单', 'button', 31, NULL, NULL, NULL, 33, 1, '编辑订单权限', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (34, 'order:delete', '删除订单', 'button', 31, NULL, NULL, NULL, 34, 1, '删除订单权限', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (40, 'user:view', '用户管理', 'menu', 0, '/user', NULL, NULL, 40, 1, '用户管理模块', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (41, 'user:list:view', '用户列表', 'menu', 40, '/user/list', NULL, NULL, 41, 1, '用户列表页面', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (42, 'user:create', '新增用户', 'button', 41, NULL, NULL, NULL, 42, 1, '新增用户权限', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (43, 'user:edit', '编辑用户', 'button', 41, NULL, NULL, NULL, 43, 1, '编辑用户权限', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (44, 'user:delete', '删除用户', 'button', 41, NULL, NULL, NULL, 44, 1, '删除用户权限', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (50, 'system:view', '系统管理', 'menu', 0, '/system', NULL, NULL, 50, 1, '系统管理模块', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (51, 'system:permission:view', '权限管理', 'menu', 50, '/system/permissions', NULL, NULL, 51, 1, '权限管理页面', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (52, 'system:permission:create', '新增权限', 'button', 51, NULL, NULL, NULL, 52, 1, '新增权限权限', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (53, 'system:permission:edit', '编辑权限', 'button', 51, NULL, NULL, NULL, 53, 1, '编辑权限权限', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (54, 'system:permission:delete', '删除权限', 'button', 51, NULL, NULL, NULL, 54, 1, '删除权限权限', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (55, 'system:config:view', '系统配置', 'menu', 50, '/system/config', NULL, NULL, 55, 1, '系统配置页面', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (56, 'system:log:view', '操作日志', 'menu', 50, '/system/operation-log', NULL, NULL, 56, 1, '操作日志页面', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (57, 'system:admin:view', '管理员管理', 'menu', 50, '/system/admin-user', NULL, NULL, 57, 1, '管理员管理页面', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (58, 'system:admin:create', '新增管理员', 'button', 57, NULL, NULL, NULL, 58, 1, '新增管理员权限', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (59, 'system:admin:edit', '编辑管理员', 'button', 57, NULL, NULL, NULL, 59, 1, '编辑管理员权限', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (60, 'system:admin:delete', '删除管理员', 'button', 57, NULL, NULL, NULL, 60, 1, '删除管理员权限', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (61, 'content:space:batchdelete', '批量删除空间', 'button', 11, '/content/spaces', NULL, NULL, 4, 1, '批量删除空间', '2025-06-14 11:18:22', '2025-06-14 11:18:50', NULL, NULL);
INSERT INTO `sys_permission` VALUES (62, 'content:space:update', '空间更新', 'menu', 11, NULL, NULL, NULL, 0, 1, NULL, '2025-06-14 11:58:27', '2025-06-14 12:06:59', NULL, NULL);
INSERT INTO `sys_permission` VALUES (63, 'content:problem:update', '问题更新', 'menu', 15, NULL, NULL, NULL, 0, 1, NULL, '2025-06-14 11:58:27', '2025-06-14 12:07:18', NULL, NULL);
INSERT INTO `sys_permission` VALUES (64, 'content:solution:update', '方案更新', 'menu', 19, NULL, NULL, NULL, 0, 1, NULL, '2025-06-14 11:58:27', '2025-06-14 12:07:45', NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
