package com.nextera.user.feign;

import com.nextera.api.auth.dto.AuthTokenDTO;
import com.nextera.api.auth.dto.LoginRequest;
import com.nextera.api.auth.dto.RegisterRequest;
import com.nextera.common.core.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 认证服务客户端熔断降级处理
 *
 * @author nextera
 * @since 2025-06-16
 */
@Slf4j
@Component
public class AuthServiceClientFallback implements AuthServiceClient {

    @Override
    public Result<AuthTokenDTO> login(LoginRequest loginRequest) {
        log.error("调用认证服务登录失败，执行降级逻辑");
        return Result.error("认证服务暂时不可用，请稍后重试");
    }

    @Override
    public Result<Void> register(RegisterRequest registerRequest) {
        log.error("调用认证服务注册失败，执行降级逻辑");
        return Result.error("注册服务暂时不可用，请稍后重试");
    }

    @Override
    public Result<Void> logout(String token) {
        log.error("调用认证服务登出失败，执行降级逻辑");
        return Result.error("登出服务暂时不可用，请稍后重试");
    }

    @Override
    public Result<AuthTokenDTO> refreshToken(String refreshToken) {
        log.error("调用认证服务刷新令牌失败，执行降级逻辑");
        return Result.error("令牌刷新服务暂时不可用，请稍后重试");
    }

    @Override
    public Result<AuthTokenDTO> validateToken(String token) {
        log.error("调用认证服务验证令牌失败，执行降级逻辑");
        return Result.error("令牌验证服务暂时不可用");
    }

    @Override
    public Result<String> getCaptcha() {
        log.error("调用认证服务获取验证码失败，执行降级逻辑");
        return Result.error("验证码服务暂时不可用，请稍后重试");
    }

    @Override
    public Result<Void> sendEmailCode(String email) {
        log.error("调用认证服务发送邮箱验证码失败，执行降级逻辑");
        return Result.error("邮箱验证码发送服务暂时不可用，请稍后重试");
    }

    @Override
    public Result<Void> resetPassword(String email, String emailCode, String newPassword) {
        log.error("调用认证服务重置密码失败，执行降级逻辑");
        return Result.error("密码重置服务暂时不可用，请稍后重试");
    }

    @Override
    public Result<Void> changePassword(Long userId, String oldPassword, String newPassword) {
        log.error("调用认证服务修改密码失败，执行降级逻辑");
        return Result.error("密码修改服务暂时不可用，请稍后重试");
    }

    @Override
    public Result<Boolean> hasPermission(Long userId, String permission) {
        log.error("调用认证服务检查权限失败，执行降级逻辑");
        return Result.error("权限检查服务暂时不可用");
    }

    @Override
    public Result<Boolean> hasRole(Long userId, String role) {
        log.error("调用认证服务检查角色失败，执行降级逻辑");
        return Result.error("角色检查服务暂时不可用");
    }
} 