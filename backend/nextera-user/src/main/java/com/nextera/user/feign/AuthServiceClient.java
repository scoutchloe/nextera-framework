package com.nextera.user.feign;

import com.nextera.api.auth.dto.AuthTokenDTO;
import com.nextera.api.auth.dto.LoginRequest;
import com.nextera.api.auth.dto.RegisterRequest;
import com.nextera.common.core.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 认证服务客户端
 * 通过OpenFeign调用认证服务
 *
 * @author nextera
 * @since 2025-06-16
 */
@FeignClient(name = "nextera-auth", 
// url = "http://localhost:7083", 
path = "/internal/auth", fallback = AuthServiceClientFallback.class)
public interface AuthServiceClient {

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return 认证令牌
     */
    @PostMapping("/login")
    Result<AuthTokenDTO> login(@RequestBody LoginRequest loginRequest);

    /**
     * 用户注册
     *
     * @param registerRequest 注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    Result<Void> register(@RequestBody RegisterRequest registerRequest);

    /**
     * 用户登出
     *
     * @param token 访问令牌
     * @return 登出结果
     */
    @PostMapping("/logout")
    Result<Void> logout(@RequestParam("token") String token);

    /**
     * 刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @return 新的认证令牌
     */
    @PostMapping("/refresh")
    Result<AuthTokenDTO> refreshToken(@RequestParam("refreshToken") String refreshToken);

    /**
     * 验证令牌
     *
     * @param token 访问令牌
     * @return 验证结果
     */
    @GetMapping("/validate")
    Result<AuthTokenDTO> validateToken(@RequestParam("token") String token);

    /**
     * 获取验证码
     *
     * @return 验证码信息
     */
    @GetMapping("/captcha")
    Result<String> getCaptcha();

    /**
     * 发送邮箱验证码
     *
     * @param email 邮箱地址
     * @return 发送结果
     */
    @PostMapping("/email/send")
    Result<Void> sendEmailCode(@RequestParam("email") String email);

    /**
     * 重置密码
     *
     * @param email 邮箱地址
     * @param emailCode 邮箱验证码
     * @param newPassword 新密码
     * @return 重置结果
     */
    @PostMapping("/password/reset")
    Result<Void> resetPassword(@RequestParam("email") String email, 
                              @RequestParam("emailCode") String emailCode, 
                              @RequestParam("newPassword") String newPassword);

    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    @PostMapping("/password/change")
    Result<Void> changePassword(@RequestParam("userId") Long userId, 
                               @RequestParam("oldPassword") String oldPassword, 
                               @RequestParam("newPassword") String newPassword);

    /**
     * 检查用户权限
     *
     * @param userId 用户ID
     * @param permission 权限标识
     * @return 是否有权限
     */
    @GetMapping("/permission/check")
    Result<Boolean> hasPermission(@RequestParam("userId") Long userId, 
                                 @RequestParam("permission") String permission);

    /**
     * 检查用户角色
     *
     * @param userId 用户ID
     * @param role 角色标识
     * @return 是否有角色
     */
    @GetMapping("/role/check")
    Result<Boolean> hasRole(@RequestParam("userId") Long userId, 
                           @RequestParam("role") String role);
} 