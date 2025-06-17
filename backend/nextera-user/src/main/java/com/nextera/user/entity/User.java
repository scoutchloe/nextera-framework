package com.nextera.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * @author nextera
 * @since 2025-06-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
//    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 用户状态：0-正常，1-禁用
     */
    private Integer status;

    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;

    /**
     * 生日
     */
    private LocalDateTime birthday;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 个人网站
     */
    private String website;

    /**
     * 所在地
     */
    private String location;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    private Integer isDeleted;
} 