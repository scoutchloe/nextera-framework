package com.nextera.user.controller;

import com.nextera.api.auth.dto.AuthTokenDTO;
import com.nextera.api.auth.dto.LoginRequest;
import com.nextera.api.auth.dto.RegisterRequest;
import com.nextera.common.core.Result;
import com.nextera.user.feign.AuthServiceClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 认证控制器
 * 提供用户认证相关的REST API
 *
 * @author nextera
 * @since 2025-06-16
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "认证管理", description = "用户认证相关接口")
public class AuthController {

    private final AuthServiceClient authServiceClient;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录获取访问令牌")
    public Result<AuthTokenDTO> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("用户登录请求，用户名: {}", loginRequest.getUsername());
        return authServiceClient.login(loginRequest);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册")
    public Result<Void> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("用户注册请求，用户名: {}", registerRequest.getUsername());
        return authServiceClient.register(registerRequest);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出，使令牌失效")
    public Result<Void> logout(@RequestHeader("Authorization") String authorization) {
        log.info("用户登出请求");
        String token = authorization.replace("Bearer ", "");
        return authServiceClient.logout(token);
    }

    /**
     * 刷新令牌
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    public Result<AuthTokenDTO> refreshToken(@RequestParam String refreshToken) {
        log.info("刷新令牌请求");
        return authServiceClient.refreshToken(refreshToken);
    }

    /**
     * 验证令牌
     */
    @GetMapping("/validate")
    @Operation(summary = "验证令牌", description = "验证访问令牌的有效性")
    public Result<AuthTokenDTO> validateToken(@RequestHeader("Authorization") String authorization) {
        log.info("验证令牌请求");
        String token = authorization.replace("Bearer ", "");
        return authServiceClient.validateToken(token);
    }

    /**
     * 获取验证码
     */
    @GetMapping("/captcha")
    @Operation(summary = "获取验证码", description = "获取图形验证码")
    public Result<String> getCaptcha() {
        log.info("获取验证码请求");
        return authServiceClient.getCaptcha();
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/email-code")
    @Operation(summary = "发送邮箱验证码", description = "向指定邮箱发送验证码")
    public Result<Void> sendEmailCode(@RequestParam String email) {
        log.info("发送邮箱验证码请求，邮箱: {}", email);
        return authServiceClient.sendEmailCode(email);
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    @Operation(summary = "重置密码", description = "通过邮箱验证码重置密码")
    public Result<Void> resetPassword(@RequestParam String email,
                                      @RequestParam String emailCode,
                                      @RequestParam String newPassword) {
        log.info("重置密码请求，邮箱: {}", email);
        return authServiceClient.resetPassword(email, emailCode, newPassword);
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    @Operation(summary = "修改密码", description = "修改用户密码")
    public Result<Void> changePassword(@RequestParam Long userId,
                                       @RequestParam String oldPassword,
                                       @RequestParam String newPassword) {
        log.info("修改密码请求，用户ID: {}", userId);
        return authServiceClient.changePassword(userId, oldPassword, newPassword);
    }

    /**
     * 检查用户权限
     */
    @GetMapping("/permission")
    @Operation(summary = "检查用户权限", description = "检查用户是否具有指定权限")
    public Result<Boolean> hasPermission(@RequestParam Long userId,
                                         @RequestParam String permission) {
        log.info("检查用户权限请求，用户ID: {}, 权限: {}", userId, permission);
        return authServiceClient.hasPermission(userId, permission);
    }

    /**
     * 检查用户角色
     */
    @GetMapping("/role")
    @Operation(summary = "检查用户角色", description = "检查用户是否具有指定角色")
    public Result<Boolean> hasRole(@RequestParam Long userId,
                                   @RequestParam String role) {
        log.info("检查用户角色请求，用户ID: {}, 角色: {}", userId, role);
        return authServiceClient.hasRole(userId, role);
    }
} 