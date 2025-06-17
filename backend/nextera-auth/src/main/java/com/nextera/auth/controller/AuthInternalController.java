package com.nextera.auth.controller;

import com.nextera.api.auth.dto.AuthTokenDTO;
import com.nextera.api.auth.dto.LoginRequest;
import com.nextera.api.auth.dto.RegisterRequest;
import com.nextera.auth.service.AuthService;
import com.nextera.common.core.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 认证内部API控制器
 * 专门用于内部服务间的OpenFeign调用
 *
 * @author Nextera
 */
@Slf4j
@RestController
@RequestMapping("/internal/auth")
@RequiredArgsConstructor
@Tag(name = "认证内部API", description = "用于内部服务间调用的认证接口")
@Validated
public class AuthInternalController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "内部用户登录", description = "内部服务调用用户登录接口")
    public Result<AuthTokenDTO> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.debug("内部服务调用用户登录: {}", loginRequest.getUsername());
        var loginResult = authService.loginInner(loginRequest);
        log.debug("内部服务调用loginResult: {}", loginResult);
        if (loginResult.isSuccess()) {
            // 转换为AuthTokenDTO
            var loginResponse = loginResult.getData();
            AuthTokenDTO authTokenDTO = new AuthTokenDTO();
            authTokenDTO.setAccessToken(loginResponse.getAccessToken());
            authTokenDTO.setRefreshToken(loginResponse.getRefreshToken());
            authTokenDTO.setExpiresIn(loginResponse.getExpiresIn());
            authTokenDTO.setTokenType("Bearer");
            return Result.success(authTokenDTO);
        } else {
            return Result.error(loginResult.getMessage());
        }
    }

    @PostMapping("/register")
    @Operation(summary = "内部用户注册", description = "内部服务调用用户注册接口")
    public Result<Void> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.debug("内部服务调用用户注册: {}", registerRequest.getUsername());
        return authService.registerInner(registerRequest);
    }

    @PostMapping("/logout")
    @Operation(summary = "内部用户登出", description = "内部服务调用用户登出接口")
    public Result<Void> logout(@Parameter(description = "访问令牌") 
                              @RequestParam("token") String token) {
        log.debug("内部服务调用用户登出");
        return authService.logout(token);
    }

    @PostMapping("/refresh")
    @Operation(summary = "内部刷新令牌", description = "内部服务调用刷新令牌接口")
    public Result<AuthTokenDTO> refreshToken(@Parameter(description = "刷新令牌") 
                                            @RequestParam("refreshToken") String refreshToken) {
        log.debug("内部服务调用刷新令牌");
        var refreshResult = authService.refreshToken(refreshToken);
        if (refreshResult.isSuccess()) {
            // 转换为AuthTokenDTO
            var loginResponse = refreshResult.getData();
            AuthTokenDTO authTokenDTO = new AuthTokenDTO();
            authTokenDTO.setAccessToken(loginResponse.getAccessToken());
            authTokenDTO.setRefreshToken(loginResponse.getRefreshToken());
            authTokenDTO.setExpiresIn(loginResponse.getExpiresIn());
            authTokenDTO.setTokenType("Bearer");
            return Result.success(authTokenDTO);
        } else {
            return Result.error(refreshResult.getMessage());
        }
    }

    @GetMapping("/validate")
    @Operation(summary = "内部验证令牌", description = "内部服务调用验证令牌接口")
    public Result<AuthTokenDTO> validateToken(@Parameter(description = "访问令牌") 
                                       @RequestParam("token") String token) {
        log.debug("内部服务调用验证令牌");
        var validateResult = authService.validateToken(token);
        if (validateResult.isSuccess() && validateResult.getData()) {
            // 如果验证成功，返回基本的token信息
            AuthTokenDTO authTokenDTO = new AuthTokenDTO();
            authTokenDTO.setAccessToken(token);
            authTokenDTO.setTokenType("Bearer");
            return Result.success(authTokenDTO);
        } else {
            return Result.error("Token验证失败");
        }
    }

    @GetMapping("/captcha")
    @Operation(summary = "内部获取验证码", description = "内部服务调用获取验证码接口")
    public Result<String> getCaptcha() {
        log.debug("内部服务调用获取验证码");
        return authService.getCaptcha("");
    }

    @PostMapping("/email/send")
    @Operation(summary = "内部发送邮箱验证码", description = "内部服务调用发送邮箱验证码接口")
    public Result<Void> sendEmailCode(@Parameter(description = "邮箱地址") 
                                     @RequestParam("email") String email) {
        log.debug("内部服务调用发送邮箱验证码: {}", email);
        // 注意：这里需要实现邮箱验证码发送逻辑
        return Result.success();
    }

    @PostMapping("/password/reset")
    @Operation(summary = "内部重置密码", description = "内部服务调用重置密码接口")
    public Result<Void> resetPassword(@Parameter(description = "邮箱地址") @RequestParam("email") String email,
                                     @Parameter(description = "邮箱验证码") @RequestParam("emailCode") String emailCode,
                                     @Parameter(description = "新密码") @RequestParam("newPassword") String newPassword) {
        log.debug("内部服务调用重置密码: {}", email);
        // 注意：这里需要实现密码重置逻辑
        return Result.success();
    }

    @PostMapping("/password/change")
    @Operation(summary = "内部修改密码", description = "内部服务调用修改密码接口")
    public Result<Void> changePassword(@Parameter(description = "用户ID") @RequestParam("userId") Long userId,
                                      @Parameter(description = "旧密码") @RequestParam("oldPassword") String oldPassword,
                                      @Parameter(description = "新密码") @RequestParam("newPassword") String newPassword) {
        log.debug("内部服务调用修改密码: {}", userId);
        // 注意：这里需要实现密码修改逻辑
        return Result.success();
    }

    @GetMapping("/permission/check")
    @Operation(summary = "内部检查用户权限", description = "内部服务调用检查用户权限接口")
    public Result<Boolean> hasPermission(@Parameter(description = "用户ID") @RequestParam("userId") Long userId,
                                        @Parameter(description = "权限标识") @RequestParam("permission") String permission) {
        log.debug("内部服务调用检查用户权限: {}, 权限: {}", userId, permission);
        // 注意：这里需要实现权限检查逻辑
        return Result.success(true);
    }

    @GetMapping("/role/check")
    @Operation(summary = "内部检查用户角色", description = "内部服务调用检查用户角色接口")
    public Result<Boolean> hasRole(@Parameter(description = "用户ID") @RequestParam("userId") Long userId,
                                  @Parameter(description = "角色标识") @RequestParam("role") String role) {
        log.debug("内部服务调用检查用户角色: {}, 角色: {}", userId, role);
        // 注意：这里需要实现角色检查逻辑
        return Result.success(true);
    }
} 