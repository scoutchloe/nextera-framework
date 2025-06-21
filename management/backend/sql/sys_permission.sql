/*
 Navicat Premium Data Transfer

 Source Server         : nextera-spring
 Source Server Type    : MySQL
 Source Server Version : 80042
 Source Host           : localhost:3306
 Source Schema         : nextera_manage

 Target Server Type    : MySQL
 Target Server Version : 80042
 File Encoding         : 65001

 Date: 21/06/2025 10:35:40
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `permission_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限编码',
  `permission_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限名称',
  `permission_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'menu' COMMENT '权限类型：menu-菜单，button-按钮',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父级权限ID，0表示顶级',
  `menu_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '菜单路径',
  `component_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '组件路径',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图标',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序序号',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权限描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_permission_code`(`permission_code`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 79 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_permission
-- ----------------------------
INSERT INTO `sys_permission` VALUES (1, 'dashboard:view', '仪表盘', 'menu', 0, '/dashboard', NULL, NULL, 1, 1, '仪表盘查看权限', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (10, 'article:view', '文章管理', 'menu', 0, '/article', '', '', 10, 1, '内容管理模块', '2025-06-14 02:58:29', '2025-06-19 09:12:24', NULL, NULL);
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
INSERT INTO `sys_permission` VALUES (57, 'system:admin:view', '管理员管理', 'menu', 50, '/system/admin', NULL, NULL, 57, 1, '管理员管理页面', '2025-06-14 02:58:29', '2025-06-19 21:53:53', NULL, NULL);
INSERT INTO `sys_permission` VALUES (58, 'system:admin:create', '新增管理员', 'button', 57, NULL, NULL, NULL, 58, 1, '新增管理员权限', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (59, 'system:admin:edit', '编辑管理员', 'button', 57, NULL, NULL, NULL, 59, 1, '编辑管理员权限', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (60, 'system:admin:delete', '删除管理员', 'button', 57, NULL, NULL, NULL, 60, 1, '删除管理员权限', '2025-06-14 02:58:29', '2025-06-14 02:58:29', NULL, NULL);
INSERT INTO `sys_permission` VALUES (65, 'article:list', '文章列表', 'menu', 10, '/article/list', '', '', 1, 1, '', '2025-06-19 09:17:38', '2025-06-19 09:17:38', NULL, NULL);
INSERT INTO `sys_permission` VALUES (66, 'article:category:list', '分类管理', 'menu', 10, '/article/category', '', '', 20, 1, '', '2025-06-19 09:19:02', '2025-06-19 09:20:28', NULL, NULL);
INSERT INTO `sys_permission` VALUES (67, 'article:tag:view', '标签管理', 'menu', 10, '/article/tag', '', '', 30, 1, '', '2025-06-19 09:19:45', '2025-06-19 09:19:59', NULL, NULL);
INSERT INTO `sys_permission` VALUES (68, 'article:edit', '文章编辑', 'button', 65, '', '', '', 1, 1, '', '2025-06-19 09:47:31', '2025-06-19 09:47:31', NULL, NULL);
INSERT INTO `sys_permission` VALUES (69, 'article:delete', '文章删除', 'button', 65, '', '', '', 2, 1, '', '2025-06-19 09:47:54', '2025-06-19 09:47:54', NULL, NULL);
INSERT INTO `sys_permission` VALUES (70, 'system:role:view', '角色管理', 'menu', 50, NULL, NULL, NULL, 3, 1, NULL, '2025-06-19 22:39:39', '2025-06-19 22:42:38', NULL, NULL);
INSERT INTO `sys_permission` VALUES (71, 'system:role:list', '角色列表', 'menu', 70, NULL, NULL, NULL, 0, 1, NULL, '2025-06-19 22:40:52', '2025-06-19 22:43:08', NULL, NULL);
INSERT INTO `sys_permission` VALUES (72, 'system:role:edit', '角色编辑', 'button', 71, '', '', '', 0, 1, '', '2025-06-19 22:44:19', '2025-06-19 22:44:19', NULL, NULL);
INSERT INTO `sys_permission` VALUES (73, 'system:role:delete', '角色删除', 'button', 71, '', '', '', 0, 1, '', '2025-06-19 22:44:38', '2025-06-19 22:44:38', NULL, NULL);
INSERT INTO `sys_permission` VALUES (74, 'system:role:search', '角色搜索', 'button', 71, '', '', '', 0, 1, '', '2025-06-19 22:45:48', '2025-06-19 22:45:48', NULL, NULL);
INSERT INTO `sys_permission` VALUES (75, 'system:role:add', '新增角色', 'button', 71, '', '', '', 0, 1, '', '2025-06-19 22:46:12', '2025-06-19 22:46:12', NULL, NULL);
INSERT INTO `sys_permission` VALUES (76, 'system:role:permission', '权限', 'button', 71, '', '', '', 0, 1, '', '2025-06-19 22:47:24', '2025-06-19 22:47:24', NULL, NULL);
INSERT INTO `sys_permission` VALUES (77, 'article:publish', '发布文章', 'button', 65, '', '', '', 0, 1, '', '2025-06-19 22:49:57', '2025-06-19 22:49:57', NULL, NULL);
INSERT INTO `sys_permission` VALUES (78, 'article:search', '搜索文章', 'button', 65, '', '', '', 0, 1, '', '2025-06-19 22:50:37', '2025-06-19 22:50:37', NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
