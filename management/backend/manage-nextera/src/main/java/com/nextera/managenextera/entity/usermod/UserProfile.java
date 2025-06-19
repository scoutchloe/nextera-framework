package com.nextera.managenextera.entity.usermod;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户资料扩展实体类
 *
 * @author Nextera
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_profile")
public class UserProfile implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户资料ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    @TableField("user_id")
    private Long userId;

    /** 真实姓名 */
    @TableField("real_name")
    private String realName;

    /** 年龄 */
    @TableField("age")
    private Integer age;

    /** 学历 */
    @TableField("education")
    private String education;

    /** 职业 */
    @TableField("profession")
    private String profession;

    /** 所在地 */
    @TableField("location")
    private String location;

    /** 个人网站 */
    @TableField("website")
    private String website;

    /** GitHub地址 */
    @TableField("github")
    private String github;

    /** 微信号 */
    @TableField("wechat")
    private String wechat;

    /** QQ号 */
    @TableField("qq")
    private String qq;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
} 