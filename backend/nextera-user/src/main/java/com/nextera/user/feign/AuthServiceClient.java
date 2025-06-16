package com.nextera.user.feign;

import com.nextera.api.auth.dto.AuthTokenDTO;
import com.nextera.api.auth.dto.LoginRequest;
import com.nextera.api.auth.dto.RegisterRequest;
import com.nextera.api.auth.service.AuthService;
import com.nextera.common.core.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/**
 * 认证服务客户端
 * 通过Dubbo调用认证服务
 *
 * @author nextera
 * @since 2025-06-16
 */
@Slf4j
@Component
public class AuthServiceClient {

    @DubboReference(version = "1.0.0", timeout = 5000, retries = 0)
    private AuthService authService;

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return 认证令牌
     */
    public Result<AuthTokenDTO> login(LoginRequest loginRequest) {
        try {
            log.info("调用认证服务进行用户登录，用户名: {}", loginRequest.getUsername());
            return authService.login(loginRequest);
        } catch (Exception e) {
            log.error("调用认证服务登录失败", e);
            return Result.error("登录服务暂时不可用，请稍后重试");
        }
    }

    /**
     * 用户注册
     *
     * @param registerRequest 注册请求
     * @return 注册结果
     */
    public Result<Void> register(RegisterRequest registerRequest) {
        try {
            log.info("调用认证服务进行用户注册，用户名: {}", registerRequest.getUsername());
            return authService.register(registerRequest);
        } catch (Exception e) {
            log.error("调用认证服务注册失败", e);
            return Result.error("注册服务暂时不可用，请稍后重试");
        }
    }

    /**
     * 用户登出
     *
     * @param token 访问令牌
     * @return 登出结果
     */
    public Result<Void> logout(String token) {
        try {
            log.info("调用认证服务进行用户登出");
            return authService.logout(token);
        } catch (Exception e) {
            log.error("调用认证服务登出失败", e);
            return Result.error("登出服务暂时不可用，请稍后重试");
        }
    }

    /**
     * 刷新令牌
     *
     * @param refreshToken 刷新令牌
     * @return 新的认证令牌
     */
    public Result<AuthTokenDTO> refreshToken(String refreshToken) {
        try {
            log.info("调用认证服务刷新令牌");
            return authService.refreshToken(refreshToken);
        } catch (Exception e) {
            log.error("调用认证服务刷新令牌失败", e);
            return Result.error("令牌刷新服务暂时不可用，请稍后重试");
        }
    }

    /**
     * 验证令牌
     *
     * @param token 访问令牌
     * @return 验证结果
     */
    public Result<AuthTokenDTO> validateToken(String token) {
        try {
            log.debug("调用认证服务验证令牌");
            return authService.validateToken(token);
        } catch (Exception e) {
            log.error("调用认证服务验证令牌失败", e);
            return Result.error("令牌验证服务暂时不可用");
        }
    }

    /**
     * 获取验证码
     *
     * @return 验证码信息
     */
    public Result<String> getCaptcha() {
        try {
            log.info("调用认证服务获取验证码");
            return authService.getCaptcha();
        } catch (Exception e) {
            log.error("调用认证服务获取验证码失败", e);
            return Result.error("验证码服务暂时不可用，请稍后重试");
        }
    }

    /**
     * 发送邮箱验证码
     *
     * @param email 邮箱地址
     * @return 发送结果
     */
    public Result<Void> sendEmailCode(String email) {
        try {
            log.info("调用认证服务发送邮箱验证码，邮箱: {}", email);
            return authService.sendEmailCode(email);
        } catch (Exception e) {
            log.error("调用认证服务发送邮箱验证码失败", e);
            return Result.error("邮箱验证码发送服务暂时不可用，请稍后重试");
        }
    }

    /**
     * 重置密码
     *
     * @param email 邮箱地址
     * @param emailCode 邮箱验证码
     * @param newPassword 新密码
     * @return 重置结果
     */
    public Result<Void> resetPassword(String email, String emailCode, String newPassword) {
        try {
            log.info("调用认证服务重置密码，邮箱: {}", email);
            return authService.resetPassword(email, emailCode, newPassword);
        } catch (Exception e) {
            log.error("调用认证服务重置密码失败", e);
            return Result.error("密码重置服务暂时不可用，请稍后重试");
        }
    }

    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    public Result<Void> changePassword(Long userId, String oldPassword, String newPassword) {
        try {
            log.info("调用认证服务修改密码，用户ID: {}", userId);
            return authService.changePassword(userId, oldPassword, newPassword);
        } catch (Exception e) {
            log.error("调用认证服务修改密码失败", e);
            return Result.error("密码修改服务暂时不可用，请稍后重试");
        }
    }

    /**
     * 检查用户权限
     *
     * @param userId 用户ID
     * @param permission 权限标识
     * @return 是否有权限
     */
    public Result<Boolean> hasPermission(Long userId, String permission) {
        try {
            log.debug("调用认证服务检查用户权限，用户ID: {}, 权限: {}", userId, permission);
            return authService.hasPermission(userId, permission);
        } catch (Exception e) {
            log.error("调用认证服务检查权限失败", e);
            return Result.error("权限检查服务暂时不可用");
        }
    }

    /**
     * 检查用户角色
     *
     * @param userId 用户ID
     * @param role 角色标识
     * @return 是否有角色
     */
    public Result<Boolean> hasRole(Long userId, String role) {
        try {
            log.debug("调用认证服务检查用户角色，用户ID: {}, 角色: {}", userId, role);
            return authService.hasRole(userId, role);
        } catch (Exception e) {
            log.error("调用认证服务检查角色失败", e);
            return Result.error("角色检查服务暂时不可用");
        }
    }
} 