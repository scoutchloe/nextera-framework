package com.nextera.managenextera.controller;

import com.nextera.managenextera.dto.SysPermissionDTO;
import com.nextera.managenextera.entity.Admin;
import com.nextera.managenextera.entity.AdminRole;
import com.nextera.managenextera.entity.SysPermission;
import com.nextera.managenextera.entity.SysRole;
import com.nextera.managenextera.entity.SysRolePermission;
import com.nextera.managenextera.service.AdminRoleService;
import com.nextera.managenextera.service.AdminService;
import com.nextera.managenextera.service.SysPermissionService;
import com.nextera.managenextera.service.SysRolePermissionService;
import com.nextera.managenextera.service.SysRoleService;
import com.nextera.managenextera.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 调试控制器 - 用于检查和修复权限数据
 */
@Slf4j
@RestController
@RequestMapping("/debug")
@RequiredArgsConstructor
public class DebugController {

    private final AdminService adminService;
    private final AdminRoleService adminRoleService;
    private final SysRoleService roleService;
    private final SysPermissionService permissionService;
    private final SysRolePermissionService rolePermissionService;

    /**
     * 检查数据库数据
     */
    @GetMapping("/check-data")
    public Result<Map<String, Object>> checkData() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 查看所有管理员
            List<Admin> admins = adminService.list();
            result.put("admins", admins);
            log.info("管理员数量: {}", admins.size());
            
            // 2. 查看所有角色
            List<SysRole> roles = roleService.list();
            result.put("roles", roles);
            log.info("角色数量: {}", roles.size());
            
            // 3. 查看管理员角色关联
            List<AdminRole> adminRoles = adminRoleService.list();
            result.put("adminRoles", adminRoles);
            log.info("管理员角色关联数量: {}", adminRoles.size());
            
            // 4. 查看所有权限
            List<SysPermission> permissions = permissionService.list();
            result.put("permissions", permissions);
            log.info("权限数量: {}", permissions.size());
            
            // 5. 查看角色权限关联
            List<SysRolePermission> rolePermissions = rolePermissionService.list();
            result.put("rolePermissions", rolePermissions);
            log.info("角色权限关联数量: {}", rolePermissions.size());
            
            // 6. 特别检查contentAdmin用户
            Admin contentAdmin = adminService.getByUsername("contentAdmin");
            if (contentAdmin != null) {
                result.put("contentAdmin", contentAdmin);
                
                // 查看contentAdmin的角色
                List<SysRole> contentAdminRoles = roleService.getRolesByAdminId(contentAdmin.getId());
                result.put("contentAdminRoles", contentAdminRoles);
                log.info("contentAdmin的角色数量: {}", contentAdminRoles.size());
                
                // 查看contentAdmin的权限
                var permissions1 = adminService.getAdminPermissions(contentAdmin.getId());
                result.put("contentAdminPermissions", permissions1);
                log.info("contentAdmin的权限数量: {}", permissions1.size());
            } else {
                result.put("contentAdmin", "用户不存在");
            }
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("检查数据失败", e);
            return Result.error("检查数据失败: " + e.getMessage());
        }
    }

    /**
     * 修复contentAdmin用户的权限数据 - 无需认证版本
     */
    @PostMapping("/fix-content-admin-public")
    public Result<String> fixContentAdminPublic() {
        return fixContentAdmin();
    }

    /**
     * 移除contentAdmin用户的删除权限 - 用于测试按钮权限控制
     */
    @PostMapping("/remove-delete-permissions")
    public Result<String> removeDeletePermissions() {
        try {
            // 1. 查找contentAdmin用户
            Admin contentAdmin = adminService.getByUsername("contentAdmin");
            if (contentAdmin == null) {
                return Result.error("contentAdmin用户不存在");
            }
            
            // 2. 查找内容管理员角色
            SysRole contentRole = roleService.list().stream()
                    .filter(role -> "CONTENT_ADMIN".equals(role.getRoleCode()))
                    .findFirst()
                    .orElse(null);
            
            if (contentRole == null) {
                return Result.error("内容管理员角色不存在");
            }
            
            // 3. 查找所有删除权限
            List<String> deletePermissions = Arrays.asList(
                "content:space:delete",
                "content:problem:delete", 
                "content:solution:delete"
            );
            
            // 4. 移除角色的删除权限
            for (String permCode : deletePermissions) {
                SysPermission permission = permissionService.list().stream()
                        .filter(p -> permCode.equals(p.getPermissionCode()))
                        .findFirst()
                        .orElse(null);
                
                if (permission != null) {
                    // 删除角色权限关联
                    rolePermissionService.list().stream()
                            .filter(rp -> rp.getRoleId().equals(contentRole.getId()) && rp.getPermissionId().equals(permission.getId()))
                            .forEach(rp -> {
                                rolePermissionService.removeById(rp.getId());
                                log.info("移除角色 {} 的权限 {}", contentRole.getRoleName(), permission.getPermissionCode());
                            });
                }
            }
            
            return Result.success("已移除contentAdmin用户的删除权限，菜单应该仍然显示，但删除按钮会消失");
        } catch (Exception e) {
            log.error("移除删除权限失败", e);
            return Result.error("移除失败: " + e.getMessage());
        }
    }

    /**
     * 修复contentAdmin用户的权限数据
     */
    @PostMapping("/fix-content-admin")
    public Result<String> fixContentAdmin() {
        try {
            // 1. 查找contentAdmin用户
            Admin contentAdmin = adminService.getByUsername("contentAdmin");
            if (contentAdmin == null) {
                return Result.error("contentAdmin用户不存在");
            }
            log.info("找到contentAdmin用户: {}", contentAdmin.getId());
            
            // 2. 查找或创建内容管理员角色
            SysRole contentRole = roleService.list().stream()
                    .filter(role -> "CONTENT_ADMIN".equals(role.getRoleCode()))
                    .findFirst()
                    .orElse(null);
            
            if (contentRole == null) {
                // 创建内容管理员角色
                contentRole = new SysRole();
                contentRole.setRoleCode("CONTENT_ADMIN");
                contentRole.setRoleName("内容管理员");
                contentRole.setStatus(1);
                contentRole.setDescription("负责内容管理的角色");
                contentRole.setCreateTime(LocalDateTime.now());
                contentRole.setUpdateTime(LocalDateTime.now());
                roleService.save(contentRole);
                log.info("创建内容管理员角色: {}", contentRole.getId());
            } else {
                log.info("找到内容管理员角色: {}", contentRole.getId());
            }
            
            // 3. 建立管理员和角色的关联
            SysRole finalContentRole = contentRole;
            AdminRole existingAdminRole = adminRoleService.list().stream()
                    .filter(ar -> ar.getAdminId().equals(contentAdmin.getId()) &&
                            ar.getRoleId().equals(finalContentRole.getId()))
                    .findFirst()
                    .orElse(null);
            
            if (existingAdminRole == null) {
                AdminRole adminRole = new AdminRole();
                adminRole.setAdminId(contentAdmin.getId());
                adminRole.setRoleId(contentRole.getId());
                adminRole.setCreateTime(LocalDateTime.now());
                adminRoleService.save(adminRole);
                log.info("创建管理员角色关联");
            } else {
                log.info("管理员角色关联已存在");
            }
            
            // 4. 确保权限数据存在
            List<String> requiredPermissions = Arrays.asList(
                "dashboard:view",
                "content:view", 
                "content:space:view",
                "content:space:create", 
                "content:space:update",
                "content:problem:view",
                "content:problem:create",
                "content:problem:update",
                "content:solution:view",
                "content:solution:create",
                "content:solution:update",
                "content:banner:view",
                "content:recommendation:view"
            );
            
            // 创建权限数据结构
            Map<String, SysPermission> permissionMap = new HashMap<>();
            
            // 根权限
            SysPermission dashboardPerm = createOrGetPermission("dashboard:view", "仪表盘查看", 0L, permissionMap);
            SysPermission contentPerm = createOrGetPermission("content:view", "内容管理", 0L, permissionMap);
            
            // 内容管理子权限
            createOrGetPermission("content:space:view", "空间查看", contentPerm.getId(), permissionMap);
            createOrGetPermission("content:space:create", "空间创建", contentPerm.getId(), permissionMap);
            createOrGetPermission("content:space:update", "空间更新", contentPerm.getId(), permissionMap);
            createOrGetPermission("content:problem:view", "问题查看", contentPerm.getId(), permissionMap);
            createOrGetPermission("content:problem:create", "问题创建", contentPerm.getId(), permissionMap);
            createOrGetPermission("content:problem:update", "问题更新", contentPerm.getId(), permissionMap);
            createOrGetPermission("content:solution:view", "方案查看", contentPerm.getId(), permissionMap);
            createOrGetPermission("content:solution:create", "方案创建", contentPerm.getId(), permissionMap);
            createOrGetPermission("content:solution:update", "方案更新", contentPerm.getId(), permissionMap);
            createOrGetPermission("content:banner:view", "轮播图查看", contentPerm.getId(), permissionMap);
            createOrGetPermission("content:recommendation:view", "推荐内容查看", contentPerm.getId(), permissionMap);
            
            // 5. 为内容管理员角色分配权限
            for (String permCode : requiredPermissions) {
                SysPermission permission = permissionMap.get(permCode);
                if (permission != null) {
                    // 检查角色权限关联是否存在
                    SysRole finalContentRole1 = contentRole;
                    boolean exists = rolePermissionService.list().stream()
                            .anyMatch(rp -> rp.getRoleId().equals(finalContentRole1.getId()) && rp.getPermissionId().equals(permission.getId()));
                    
                    if (!exists) {
                        SysRolePermission rolePermission = new SysRolePermission();
                        rolePermission.setRoleId(contentRole.getId());
                        rolePermission.setPermissionId(permission.getId());
                        rolePermission.setCreateTime(LocalDateTime.now());
                        rolePermissionService.save(rolePermission);
                        log.info("为角色 {} 添加权限 {}", contentRole.getRoleName(), permission.getPermissionCode());
                    }
                }
            }
            
            return Result.success("contentAdmin用户权限修复完成");
        } catch (Exception e) {
            log.error("修复contentAdmin权限失败", e);
            return Result.error("修复失败: " + e.getMessage());
        }
    }
    
    private SysPermission createOrGetPermission(String permissionCode, String permissionName, Long parentId, Map<String, SysPermission> permissionMap) {
        // 先从数据库查找
        SysPermission existing = permissionService.list().stream()
                .filter(p -> permissionCode.equals(p.getPermissionCode()))
                .findFirst()
                .orElse(null);
        
        if (existing != null) {
            permissionMap.put(permissionCode, existing);
            return existing;
        }
        
        // 创建新权限
        SysPermission permission = new SysPermission();
        permission.setPermissionCode(permissionCode);
        permission.setPermissionName(permissionName);
        permission.setParentId(parentId);
        permission.setStatus(1);
        permission.setCreateTime(LocalDateTime.now());
        permission.setUpdateTime(LocalDateTime.now());
        permissionService.save(permission);
        
        permissionMap.put(permissionCode, permission);
        log.info("创建权限: {} - {}", permissionCode, permissionName);
        return permission;
    }

    /**
     * 快速检查contentAdmin权限状态
     */
    @GetMapping("/check-content-admin")
    public Result<Map<String, Object>> checkContentAdmin() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 1. 查找contentAdmin用户
            Admin contentAdmin = adminService.getByUsername("contentAdmin");
            if (contentAdmin == null) {
                result.put("error", "contentAdmin用户不存在");
                return Result.error("contentAdmin用户不存在");
            }
            
            result.put("userId", contentAdmin.getId());
            result.put("username", contentAdmin.getUsername());
            
            // 2. 查看用户角色
            List<SysRole> roles = roleService.getRolesByAdminId(contentAdmin.getId());
            result.put("roles", roles.stream().map(r -> r.getRoleName()).collect(Collectors.toList()));
            
            // 3. 查看用户权限
            List<SysPermissionDTO> permissions = adminService.getAdminPermissions(contentAdmin.getId());
            List<String> permissionCodes = permissions.stream()
                    .map(SysPermissionDTO::getPermissionCode)
                    .collect(Collectors.toList());
            result.put("permissions", permissionCodes);
            
            // 4. 检查关键权限
            Map<String, Boolean> keyPermissions = new HashMap<>();
            keyPermissions.put("dashboard:view", permissionCodes.contains("dashboard:view"));
            keyPermissions.put("article:view", permissionCodes.contains("article:view"));
            keyPermissions.put("article:list", permissionCodes.contains("article:list"));
            keyPermissions.put("article:category:list", permissionCodes.contains("article:category:list"));
            keyPermissions.put("article:tag:list", permissionCodes.contains("article:tag:list"));
            keyPermissions.put("system:view", permissionCodes.contains("system:view"));
            keyPermissions.put("user:view", permissionCodes.contains("user:view"));
            
            result.put("keyPermissions", keyPermissions);
            
            // 5. 建议
            List<String> suggestions = new ArrayList<>();
            if (!keyPermissions.get("article:view")) {
                suggestions.add("缺少 article:view 权限，文章管理菜单不会显示");
            }
            if (!keyPermissions.get("dashboard:view")) {
                suggestions.add("缺少 dashboard:view 权限，仪表盘不会显示");
            }
            if (keyPermissions.get("system:view")) {
                suggestions.add("有 system:view 权限，系统管理菜单会显示（可能不应该有）");
            }
            if (keyPermissions.get("user:view")) {
                suggestions.add("有 user:view 权限，用户管理菜单会显示（可能不应该有）");
            }
            
            result.put("suggestions", suggestions);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("检查contentAdmin失败", e);
            return Result.error("检查失败: " + e.getMessage());
        }
    }
} 