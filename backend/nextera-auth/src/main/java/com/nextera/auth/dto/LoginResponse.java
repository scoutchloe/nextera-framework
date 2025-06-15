package com.nextera.auth.dto;

import lombok.Data;

/**
 * 登录响应DTO
 *
 * @author Nextera
 */
@Data
public class LoginResponse {

    /** 访问令牌 */
    private String accessToken;

    /** 刷新令牌 */
    private String refreshToken;

    /** 令牌类型 */
    private String tokenType = "Bearer";

    /** 过期时间（秒） */
    private Long expiresIn;

    /** 用户信息 */
    private UserInfo userInfo;

    @Data
    public static class UserInfo {
        /** 用户ID */
        private Long id;

        /** 用户名 */
        private String username;

        /** 昵称 */
        private String nickname;

        /** 邮箱 */
        private String email;

        /** 头像 */
        private String avatar;

        /** 角色列表 */
        private String[] roles;

        /** 权限列表 */
        private String[] permissions;
    }
} 