package com.nextera.auth.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 登录请求DTO
 *
 * @author Nextera
 */
@Data
public class LoginRequest {

    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20之间")
    private String username;

    /** 密码 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20之间")
    private String password;

    /** 验证码 */
    @NotBlank(message = "验证码不能为空")
    private String captcha;

    /** 验证码唯一标识 */
    @NotBlank(message = "验证码标识不能为空")
    private String uuid;
} 