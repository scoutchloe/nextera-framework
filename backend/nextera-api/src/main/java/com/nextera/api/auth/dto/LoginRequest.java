package com.nextera.api.auth.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 登录请求DTO
 *
 * @author nextera
 * @since 2025-06-16
 */
@Data
public class LoginRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名或邮箱
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 验证码
     */
    private String captcha;

    /**
     * 验证码key
     */
    private String captchaKey;

    /**
     * 记住我
     */
    private Boolean rememberMe = false;
} 