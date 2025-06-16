package com.nextera.api.auth.service;

import com.nextera.api.auth.dto.AuthTokenDTO;
import com.nextera.api.auth.dto.LoginRequest;
import com.nextera.api.auth.dto.RegisterRequest;
import com.nextera.common.core.Result;

/**
 * 认证服务接口
 *
 * @author nextera
 * @since 2025-06-16
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return 认证令牌
     */
    Result<AuthTokenDTO> login(LoginRequest loginRequest);

    /**
     * 用户注册
     *
     * @param registerRequest 注册请求
     * @return 注册结果
     */
    Result<Void> register(RegisterRequest registerRequest);

    /**
     * 用户登出
     *
     * @param token 访问令牌
     * @return 登出结果
     */
    Result<Void> logout(String token);

    /**
     * 刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @return 新的认证令牌
     */
    Result<AuthTokenDTO> refreshToken(String refreshToken);

    /**
     * 验证令牌
     *
     * @param token 访问令牌
     * @return 验证结果
     */
    Result<AuthTokenDTO> validateToken(String token);

    /**
     * 获取验证码
     *
     * @return 验证码信息
     */
    Result<String> getCaptcha();

    /**
     * 发送邮箱验证码
     *
     * @param email 邮箱地址
     * @return 发送结果
     */
    Result<Void> sendEmailCode(String email);

    /**
     * 重置密码
     *
     * @param email 邮箱地址
     * @param emailCode 邮箱验证码
     * @param newPassword 新密码
     * @return 重置结果
     */
    Result<Void> resetPassword(String email, String emailCode, String newPassword);

    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    Result<Void> changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 检查用户权限
     *
     * @param userId 用户ID
     * @param permission 权限标识
     * @return 是否有权限
     */
    Result<Boolean> hasPermission(Long userId, String permission);

    /**
     * 检查用户角色
     *
     * @param userId 用户ID
     * @param role 角色标识
     * @return 是否有角色
     */
    Result<Boolean> hasRole(Long userId, String role);
} 