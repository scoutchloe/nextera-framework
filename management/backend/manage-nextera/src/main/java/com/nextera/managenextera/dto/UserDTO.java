package com.nextera.managenextera.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户DTO
 *
 * @author nextera
 * @since 2025-06-19
 */
@Data
@Schema(description = "用户DTO")
public class UserDTO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "用户状态：0-正常，1-禁用")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "性别：0-未知，1-男，2-女")
    private Integer gender;

    @Schema(description = "性别名称")
    private String genderName;

    @Schema(description = "生日")
    private LocalDateTime birthday;

    @Schema(description = "个人简介")
    private String bio;

    @Schema(description = "个人网站")
    private String website;

    @Schema(description = "所在地")
    private String location;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;
} 