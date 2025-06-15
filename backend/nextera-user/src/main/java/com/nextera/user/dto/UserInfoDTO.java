package com.nextera.user.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息DTO
 *
 * @author Nextera
 */
@Data
public class UserInfoDTO {

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 头像 */
    private String avatar;

    /** 性别 (0-未知 1-男 2-女) */
    private String gender;

    /** 生日 */
    private LocalDateTime birthday;

    /** 个人简介 */
    private String bio;

    /** 状态 (0-正常 1-停用 2-删除) */
    private String status;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 用户扩展资料 */
    private UserProfileDTO profile;

    @Data
    public static class UserProfileDTO {
        /** 真实姓名 */
        private String realName;

        /** 年龄 */
        private Integer age;

        /** 学历 */
        private String education;

        /** 职业 */
        private String profession;

        /** 所在地 */
        private String location;

        /** 个人网站 */
        private String website;

        /** GitHub地址 */
        private String github;

        /** 微信号 */
        private String wechat;

        /** QQ号 */
        private String qq;
    }
} 