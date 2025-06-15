package com.nextera.article.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 文章创建请求DTO
 *
 * @author nextera
 * @since 2025-06-16
 */
@Data
@Schema(description = "文章创建请求")
public class ArticleCreateRequest {

    @Schema(description = "文章标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "文章标题不能为空")
    private String title;

    @Schema(description = "文章内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "文章内容不能为空")
    private String content;

    @Schema(description = "文章摘要")
    private String summary;

    @Schema(description = "分类ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @Schema(description = "文章状态：0-草稿，1-已发布，2-已下架")
    private Integer status = 0;

    @Schema(description = "是否置顶：0-否，1-是")
    private Integer isTop = 0;

    @Schema(description = "是否推荐：0-否，1-是")
    private Integer isRecommend = 0;

    @Schema(description = "标签（用逗号分隔）")
    private String tags;

    @Schema(description = "封面图片URL")
    private String coverImage;

    @Schema(description = "SEO关键词")
    private String seoKeywords;

    @Schema(description = "SEO描述")
    private String seoDescription;
} 