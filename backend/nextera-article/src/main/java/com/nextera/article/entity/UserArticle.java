package com.nextera.article.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户文章关联记录实体类
 *
 * @author nextera
 * @since 2025-06-16
 */
@Data
@TableName("user_article")
@Schema(description = "用户文章关联记录")
public class UserArticle {

    @Schema(description = "记录ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "用户ID")
    @TableField("user_id")
    private Long userId;

    @Schema(description = "用户名")
    @TableField("username")
    private String username;

    @Schema(description = "文章ID")
    @TableField("article_id")
    private Long articleId;

    @Schema(description = "文章标题")
    @TableField("article_title")
    private String articleTitle;

    @Schema(description = "操作类型：CREATE-创建，UPDATE-编辑，PUBLISH-发布，DELETE-删除")
    @TableField("action_type")
    private String actionType;

    @Schema(description = "操作描述")
    @TableField("action_desc")
    private String actionDesc;

    @Schema(description = "IP地址")
    @TableField("ip_address")
    private String ipAddress;

    @Schema(description = "用户代理")
    @TableField("user_agent")
    private String userAgent;

    @Schema(description = "操作时间")
    @TableField(value = "action_time", fill = FieldFill.INSERT)
    private LocalDateTime actionTime;

    @Schema(description = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "是否删除：0-否，1-是")
    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
} 