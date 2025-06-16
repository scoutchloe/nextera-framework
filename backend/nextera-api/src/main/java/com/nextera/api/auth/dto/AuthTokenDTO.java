package com.nextera.api.auth.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 认证令牌DTO
 *
 * @author nextera
 * @since 2025-06-16
 */
@Data
public class AuthTokenDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * 令牌类型
     */
    private String tokenType = "Bearer";

    /**
     * 过期时间（秒）
     */
    private Long expiresIn;

    /**
     * 过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 权限列表
     */
    private String[] permissions;

    /**
     * 角色列表
     */
    private String[] roles;
} 