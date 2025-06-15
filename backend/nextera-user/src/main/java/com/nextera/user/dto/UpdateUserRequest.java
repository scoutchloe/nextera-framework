package com.nextera.user.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 用户更新请求DTO
 *
 * @author Nextera
 */
@Data
public class UpdateUserRequest {

    /** 昵称 */
    @Size(min = 2, max = 20, message = "昵称长度必须在2-20之间")
    private String nickname;

    /** 邮箱 */
    @Email(message = "邮箱格式不正确")
    private String email;

    /** 手机号 */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 头像 */
    private String avatar;

    /** 性别 (0-未知 1-男 2-女) */
    @Pattern(regexp = "^[012]$", message = "性别值只能是0、1或2")
    private String gender;

    /** 生日 */
    private LocalDateTime birthday;

    /** 个人简介 */
    @Size(max = 500, message = "个人简介长度不能超过500字符")
    private String bio;

    /** 用户扩展资料 */
    private UserProfileRequest profile;

    @Data
    public static class UserProfileRequest {
        /** 真实姓名 */
        @Size(max = 50, message = "真实姓名长度不能超过50字符")
        private String realName;

        /** 年龄 */
        private Integer age;

        /** 学历 */
        @Size(max = 50, message = "学历长度不能超过50字符")
        private String education;

        /** 职业 */
        @Size(max = 100, message = "职业长度不能超过100字符")
        private String profession;

        /** 所在地 */
        @Size(max = 100, message = "所在地长度不能超过100字符")
        private String location;

        /** 个人网站 */
        @Size(max = 255, message = "个人网站长度不能超过255字符")
        private String website;

        /** GitHub地址 */
        @Size(max = 255, message = "GitHub地址长度不能超过255字符")
        private String github;

        /** 微信号 */
        @Size(max = 50, message = "微信号长度不能超过50字符")
        private String wechat;

        /** QQ号 */
        @Size(max = 20, message = "QQ号长度不能超过20字符")
        private String qq;
    }
}