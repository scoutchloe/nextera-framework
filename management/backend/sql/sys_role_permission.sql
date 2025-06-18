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

 Date: 18/06/2025 15:38:42
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `permission_id` bigint(20) NOT NULL COMMENT '权限ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` bigint(20) NULL DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_permission`(`role_id`, `permission_id`) USING BTREE,
  INDEX `idx_role_id`(`role_id`) USING BTREE,
  INDEX `idx_permission_id`(`permission_id`) USING BTREE,
  CONSTRAINT `fk_role_permission_permission` FOREIGN KEY (`permission_id`) REFERENCES `sys_permission` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_role_permission_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 244 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色权限关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_permission
-- ----------------------------
INSERT INTO `sys_role_permission` VALUES (1, 1, 1, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (2, 1, 10, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (3, 1, 11, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (4, 1, 12, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (5, 1, 13, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (6, 1, 14, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (7, 1, 15, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (8, 1, 16, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (9, 1, 17, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (10, 1, 18, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (11, 1, 19, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (12, 1, 20, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (13, 1, 21, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (14, 1, 22, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (15, 1, 23, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (16, 1, 24, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (17, 1, 30, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (18, 1, 31, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (19, 1, 32, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (20, 1, 33, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (21, 1, 34, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (22, 1, 40, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (23, 1, 41, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (24, 1, 42, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (25, 1, 43, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (26, 1, 44, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (27, 1, 50, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (28, 1, 51, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (29, 1, 52, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (30, 1, 53, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (31, 1, 54, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (32, 1, 55, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (33, 1, 56, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (34, 1, 57, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (35, 1, 58, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (36, 1, 59, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (37, 1, 60, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (80, 3, 1, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (81, 3, 30, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (82, 3, 31, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (83, 3, 32, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (84, 3, 33, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (85, 3, 34, '2025-06-14 02:58:29', NULL);
INSERT INTO `sys_role_permission` VALUES (224, 2, 62, '2025-06-14 14:36:25', NULL);
INSERT INTO `sys_role_permission` VALUES (225, 2, 63, '2025-06-14 14:36:25', NULL);
INSERT INTO `sys_role_permission` VALUES (226, 2, 64, '2025-06-14 14:36:25', NULL);
INSERT INTO `sys_role_permission` VALUES (227, 2, 1, '2025-06-14 14:36:25', NULL);
INSERT INTO `sys_role_permission` VALUES (228, 2, 61, '2025-06-14 14:36:25', NULL);
INSERT INTO `sys_role_permission` VALUES (229, 2, 10, '2025-06-14 14:36:25', NULL);
INSERT INTO `sys_role_permission` VALUES (230, 2, 11, '2025-06-14 14:36:25', NULL);
INSERT INTO `sys_role_permission` VALUES (231, 2, 12, '2025-06-14 14:36:25', NULL);
INSERT INTO `sys_role_permission` VALUES (232, 2, 13, '2025-06-14 14:36:25', NULL);
INSERT INTO `sys_role_permission` VALUES (233, 2, 15, '2025-06-14 14:36:25', NULL);
INSERT INTO `sys_role_permission` VALUES (234, 2, 16, '2025-06-14 14:36:25', NULL);
INSERT INTO `sys_role_permission` VALUES (235, 2, 17, '2025-06-14 14:36:25', NULL);
INSERT INTO `sys_role_permission` VALUES (236, 2, 18, '2025-06-14 14:36:25', NULL);
INSERT INTO `sys_role_permission` VALUES (237, 2, 19, '2025-06-14 14:36:25', NULL);
INSERT INTO `sys_role_permission` VALUES (238, 2, 20, '2025-06-14 14:36:25', NULL);
INSERT INTO `sys_role_permission` VALUES (239, 2, 21, '2025-06-14 14:36:25', NULL);
INSERT INTO `sys_role_permission` VALUES (240, 2, 22, '2025-06-14 14:36:25', NULL);
INSERT INTO `sys_role_permission` VALUES (241, 2, 23, '2025-06-14 14:36:25', NULL);
INSERT INTO `sys_role_permission` VALUES (242, 2, 24, '2025-06-14 14:36:25', NULL);
INSERT INTO `sys_role_permission` VALUES (243, 2, 14, '2025-06-14 14:36:25', NULL);

SET FOREIGN_KEY_CHECKS = 1;
