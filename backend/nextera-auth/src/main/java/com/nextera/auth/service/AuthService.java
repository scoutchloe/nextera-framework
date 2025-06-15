package com.nextera.auth.service;

import com.nextera.auth.dto.LoginRequest;
import com.nextera.auth.dto.LoginResponse;
import com.nextera.auth.dto.RegisterRequest;
import com.nextera.common.core.Result;

/**
 * 认证服务接口
 *
 * @author Nextera
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    Result<LoginResponse> login(LoginRequest loginRequest);

    /**
     * 用户注册
     *
     * @param registerRequest 注册请求
     * @return 注册结果
     */
    Result<Void> register(RegisterRequest registerRequest);

    /**
     * 刷新Token
     *
     * @param refreshToken 刷新Token
     * @return 新的Token信息
     */
    Result<LoginResponse> refreshToken(String refreshToken);

    /**
     * 退出登录
     *
     * @param token 访问Token
     * @return 退出结果
     */
    Result<Void> logout(String token);

    /**
     * 验证Token
     *
     * @param token 访问Token
     * @return 验证结果
     */
    Result<Boolean> validateToken(String token);

    /**
     * 获取验证码
     *
     * @param uuid 验证码唯一标识
     * @return 验证码图片base64编码
     */
    Result<String> getCaptcha(String uuid);
} 