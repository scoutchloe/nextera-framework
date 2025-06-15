package com.nextera.article.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章DTO
 *
 * @author nextera
 * @since 2025-06-16
 */
@Data
@Schema(description = "文章信息")
public class ArticleDTO {

    @Schema(description = "文章ID")
    private Long id;

    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "文章内容")
    private String content;

    @Schema(description = "文章摘要")
    private String summary;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "作者ID")
    private Long authorId;

    @Schema(description = "作者姓名")
    private String authorName;

    @Schema(description = "文章状态：0-草稿，1-已发布，2-已下架")
    private Integer status;

    @Schema(description = "阅读次数")
    private Long viewCount = 0L;

    @Schema(description = "点赞次数")
    private Long likeCount = 0L;

    @Schema(description = "评论次数")
    private Long commentCount = 0L;

    @Schema(description = "是否置顶：0-否，1-是")
    private Integer isTop;

    @Schema(description = "是否推荐：0-否，1-是")
    private Integer isRecommend;

    @Schema(description = "标签（用逗号分隔）")
    private String tags;

    @Schema(description = "封面图片URL")
    private String coverImage;

    @Schema(description = "SEO关键词")
    private String seoKeywords;

    @Schema(description = "SEO描述")
    private String seoDescription;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
} 