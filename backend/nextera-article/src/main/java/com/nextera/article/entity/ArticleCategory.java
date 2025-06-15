package com.nextera.article.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章分类实体类
 *
 * @author nextera
 * @since 2025-06-16
 */
@Data
@TableName("article_category")
@Schema(description = "文章分类")
public class ArticleCategory {

    @Schema(description = "分类ID")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "分类名称")
    @TableField("name")
    private String name;

    @Schema(description = "分类描述")
    @TableField("description")
    private String description;

    @Schema(description = "父分类ID")
    @TableField("parent_id")
    private Long parentId;

    @Schema(description = "排序号")
    @TableField("sort_order")
    private Integer sortOrder;

    @Schema(description = "分类状态：0-禁用，1-启用")
    @TableField("status")
    private Integer status;

    @Schema(description = "分类图标")
    @TableField("icon")
    private String icon;

    @Schema(description = "分类颜色")
    @TableField("color")
    private String color;

    @Schema(description = "文章数量")
    @TableField("article_count")
    private Long articleCount;

    @Schema(description = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "创建者ID")
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    @Schema(description = "更新者ID")
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    @Schema(description = "是否删除：0-否，1-是")
    @TableField("is_deleted")
    @TableLogic
    private Integer isDeleted;
} 