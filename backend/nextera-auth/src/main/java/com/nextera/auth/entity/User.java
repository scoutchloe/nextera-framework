package com.nextera.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * @author Nextera
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 用户名 */
    @TableField("username")
    private String username;

    /** 密码 */
    @TableField("password")
    private String password;

    /** 昵称 */
    @TableField("nickname")
    private String nickname;

    /** 邮箱 */
    @TableField("email")
    private String email;

    /** 手机号 */
    @TableField("phone")
    private String phone;

    /** 头像 */
    @TableField("avatar")
    private String avatar;

    /** 性别 (0-未知 1-男 2-女) */
    @TableField("gender")
    private String gender;

    /** 生日 */
    @TableField("birthday")
    private LocalDateTime birthday;

    /** 个人简介 */
    @TableField("bio")
    private String bio;

    /** 状态 (0-正常 1-停用 2-删除) */
    @TableField("status")
    private String status;

    /** 最后登录时间 */
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    /** 最后登录IP */
    @TableField("last_login_ip")
    private String lastLoginIp;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 创建者 */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    /** 更新者 */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /** 删除标志 (0-存在 1-删除) */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /** 备注 */
    @TableField("remark")
    private String remark;
} 