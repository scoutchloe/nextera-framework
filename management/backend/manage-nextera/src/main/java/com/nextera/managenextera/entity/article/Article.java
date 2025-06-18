package com.nextera.managenextera.entity.article;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章实体类
 *
 * @author nextera
 * @since 2025-06-16
 */
@Data
@TableName("article")
@Schema(description = "文章")
public class Article {

    @Schema(description = "文章ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "文章标题")
    @TableField("title")
    private String title;

    @Schema(description = "文章内容")
    @TableField("content")
    private String content;

    @Schema(description = "文章摘要")
    @TableField("summary")
    private String summary;

    @Schema(description = "分类ID")
    @TableField("category_id")
    private Long categoryId;

    @Schema(description = "作者ID")
    @TableField("author_id")
    private Long authorId;

    @Schema(description = "作者用户名")
    @TableField("author_name")
    private String authorName;

    @Schema(description = "文章状态：0-草稿，1-已发布，2-已下架")
    @TableField("status")
    private Integer status;

    @Schema(description = "阅读次数")
    @TableField("view_count")
    private Integer viewCount;

    @Schema(description = "点赞次数")
    @TableField("like_count")
    private Integer likeCount;

    @Schema(description = "评论次数")
    @TableField("comment_count")
    private Long commentCount;

    @Schema(description = "是否置顶：0-否，1-是")
    @TableField("is_top")
    private Integer isTop;

    @Schema(description = "是否推荐：0-否，1-是")
    @TableField("is_recommend")
    private Integer isRecommend;

    @Schema(description = "标签（用逗号分隔）")
    @TableField("tags")
    private String tags;

    @Schema(description = "封面图片URL")
    @TableField("cover_image")
    private String coverImage;


    @Schema(description = "创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "发布时间")
    @TableField("publish_time")
    private LocalDateTime publishTime;

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