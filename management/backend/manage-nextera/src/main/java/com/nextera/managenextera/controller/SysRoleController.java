package com.nextera.managenextera.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nextera.managenextera.annotation.AntiReplay;
import com.nextera.managenextera.dto.RolePermissionDTO;
import com.nextera.managenextera.dto.SysRoleDTO;
import com.nextera.managenextera.entity.SysRole;
import com.nextera.managenextera.service.SysRoleService;
import com.nextera.managenextera.common.Result;
import com.nextera.managenextera.interceptor.CachedBodyHttpServletRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统角色管理Controller
 */
@Tag(name = "角色管理", description = "系统角色管理相关接口")
@RestController
@RequestMapping("/system/role")
@Slf4j
public class SysRoleController {

    @Autowired
    private SysRoleService roleService;

    @Operation(summary = "分页查询角色", description = "分页查询角色列表")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:role:list')")
    public Result<IPage<SysRole>> getRolePage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) Integer status) {

        IPage<SysRole> result = roleService.getRolePage(current, size, roleName, roleCode, status);

        return Result.success(result);
    }

    @Operation(summary = "获取所有角色", description = "获取所有启用状态的角色")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:role:list')")
    public Result<List<SysRole>> getAllRoles() {

        List<SysRole> roles = roleService.list(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getStatus, 1)
                .orderByDesc(SysRole::getCreateTime));


        return Result.success(roles);

    }

    @Operation(summary = "根据ID获取角色详情", description = "根据角色ID获取角色详细信息，包含权限信息")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:list')")
    public ResponseEntity<Map<String, Object>> getRoleById(
            @Parameter(description = "角色ID") @PathVariable Long id) {

        SysRole role = roleService.getRoleWithPermissions(id);
        Map<String, Object> result = new HashMap<>();
        if (role != null) {
            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", role);
        } else {
            result.put("code", 404);
            result.put("message", "角色不存在");
        }
        return ResponseEntity.ok(result);

    }

    @Operation(summary = "添加角色", description = "添加新的角色（需要签名验证）")
    @PostMapping
    @PreAuthorize("hasAuthority('system:role:add')")
    public ResponseEntity<Map<String, Object>> addRole(
            @Validated @RequestBody SysRoleDTO roleDTO,
            HttpServletRequest request) {

        log.info("==== 角色新增请求开始 ====");
        log.info("请求URI: {}", request.getRequestURI());
        log.info("请求方法: {}", request.getMethod());
        log.info("Content-Type: {}", request.getContentType());
        log.info("角色信息 - 名称: {}, 编码: {}, 状态: {}", roleDTO.getRoleName(), roleDTO.getRoleCode(), roleDTO.getStatus());
        
        // 检查签名头部
        String signature = request.getHeader("X-Signature");
        String timestamp = request.getHeader("X-Timestamp");
        log.info("签名头部 - X-Signature: {}, X-Timestamp: {}", 
            signature != null ? "已提供" : "缺失", 
            timestamp != null ? "已提供" : "缺失"
        );
        
        // 检查是否通过了签名验证过滤器
        Boolean signatureVerified = (Boolean) request.getAttribute("signatureVerified");
        
        if (signatureVerified != null && signatureVerified) {
            log.info("✓ 签名验证过滤器已处理，验证通过");
        } else {
            // 检查是否有签名头部但过滤器未处理
            if (signature != null && timestamp != null) {
                log.warn("⚠ 检测到签名头部但过滤器未处理，可能配置有误");
            } else {
                log.warn("⚠ 未检测到签名头部，过滤器未启用或请求未经过过滤器");
            }
        }

        try {
            boolean success = roleService.addRole(roleDTO);
            Map<String, Object> result = new HashMap<>();
            
            if (success) {
                result.put("code", 200);
                result.put("message", "添加成功");
                log.info("✓ 角色添加成功: {}", roleDTO.getRoleName());
            } else {
                result.put("code", 500);
                result.put("message", "添加失败");
                log.error("✗ 角色添加失败: {}", roleDTO.getRoleName());
            }
            
            log.info("==== 角色新增请求结束 ====");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("角色新增过程中发生异常", e);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "系统异常: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    @Operation(summary = "更新角色", description = "更新角色信息（支持混合加密传输）")
    @PutMapping
    @PreAuthorize("hasAuthority('system:role:edit')")
    public ResponseEntity<Map<String, Object>> updateRole(
            @Validated @RequestBody(required = false) SysRoleDTO roleDTO,
            HttpServletRequest request) {

        log.info("==== 角色更新请求开始 ====");
        log.info("请求URI: {}", request.getRequestURI());
        log.info("请求方法: {}", request.getMethod());
        log.info("Content-Type: {}", request.getContentType());
        log.info("角色信息 - ID: {}, 名称: {}, 编码: {}, 状态: {}", 
                roleDTO.getId(), roleDTO.getRoleName(), roleDTO.getRoleCode(), roleDTO.getStatus());
        
        // 检查加密头部
        String encryptedHeader = request.getHeader("X-Encrypted");
        String encryptionTypeHeader = request.getHeader("X-Encryption-Type");
        log.info("加密头部 - X-Encrypted: {}, X-Encryption-Type: {}", encryptedHeader, encryptionTypeHeader);
        
        // 检查是否通过了混合加密解密过滤器
        Boolean hybridDecrypted = (Boolean) request.getAttribute("hybridDecrypted");
        String originalEncryptedBody = (String) request.getAttribute("originalEncryptedBody");
        
        if (hybridDecrypted != null && hybridDecrypted) {
            log.info("✓ 混合加密解密过滤器已处理，解密成功");
            if (originalEncryptedBody != null) {
                log.debug("✓ 原始加密数据长度: {}", originalEncryptedBody.length());
            }
                 } else {
             // 检查是否有加密头部但过滤器未处理
             if ("true".equals(encryptedHeader) && "hybrid".equals(encryptionTypeHeader)) {
                 log.warn("⚠ 检测到混合加密标识但过滤器未处理，可能配置有误");
             } else {
                 log.info("ℹ 普通请求，未使用混合加密");
             }
         }

        try {
            boolean success = roleService.updateRole(roleDTO);
            Map<String, Object> result = new HashMap<>();
            
            if (success) {
                result.put("code", 200);
                result.put("message", "更新成功");
                log.info("✓ 角色更新成功: ID={}, 名称={}", roleDTO.getId(), roleDTO.getRoleName());
            } else {
                result.put("code", 500);
                result.put("message", "更新失败");
                log.error("✗ 角色更新失败: ID={}, 名称={}", roleDTO.getId(), roleDTO.getRoleName());
            }
            
            log.info("==== 角色更新请求结束 ====");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("角色更新过程中发生异常", e);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "系统异常: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    @Operation(summary = "删除角色", description = "根据ID删除角色（防重放保护）")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:delete')")
    @AntiReplay(timeWindow = 5 * 60 * 1000, enableNonce = true) // 5分钟时间窗口，启用nonce验证
    public ResponseEntity<Map<String, Object>> deleteRole(
            @Parameter(description = "角色ID") @PathVariable Long id,
            HttpServletRequest request) {

        log.info("==== 角色删除请求开始 ====");
        log.info("请求URI: {}", request.getRequestURI());
        log.info("请求方法: {}", request.getMethod());
        log.info("角色ID: {}", id);
        
        // 检查防重放头部
        String timestamp = request.getHeader("X-Timestamp");
        String nonce = request.getHeader("X-Nonce");
        log.info("防重放头部 - X-Timestamp: {}, X-Nonce: {}", 
            timestamp != null ? "已提供" : "缺失", 
            nonce != null ? "已提供" : "缺失"
        );
        
        try {
            boolean success = roleService.deleteRole(id);
            Map<String, Object> result = new HashMap<>();
            
            if (success) {
                result.put("code", 200);
                result.put("message", "删除成功");
                log.info("✓ 角色删除成功: ID={}", id);
            } else {
                result.put("code", 500);
                result.put("message", "删除失败");
                log.error("✗ 角色删除失败: ID={}", id);
            }
            
            log.info("==== 角色删除请求结束 ====");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("角色删除过程中发生异常", e);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "系统异常: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    @Operation(summary = "分配角色权限", description = "为角色分配权限")
    @PostMapping("/assign-permissions")
    @PreAuthorize("hasAuthority('system:role:auth')")
    public Result<Boolean> assignRolePermissions(
            @Validated @RequestBody RolePermissionDTO rolePermissionDTO) {

        boolean success = roleService.assignRolePermissions(rolePermissionDTO);

        return Result.success(success);
    }
} 