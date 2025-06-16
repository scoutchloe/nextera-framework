package com.nextera.api.article.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 文章创建请求DTO
 *
 * @author nextera
 * @since 2025-06-16
 */
@Data
public class ArticleCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文章标题
     */
    @NotBlank(message = "文章标题不能为空")
    private String title;

    /**
     * 文章内容
     */
    @NotBlank(message = "文章内容不能为空")
    private String content;

    /**
     * 文章摘要
     */
    private String summary;

    /**
     * 分类ID
     */
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    /**
     * 文章状态：0-草稿，1-已发布，2-已下架
     */
    private Integer status = 0;

    /**
     * 是否置顶：0-否，1-是
     */
    private Integer isTop = 0;

    /**
     * 是否推荐：0-否，1-是
     */
    private Integer isRecommend = 0;

    /**
     * 标签（用逗号分隔）
     */
    private String tags;

    /**
     * 封面图片URL
     */
    private String coverImage;

    /**
     * SEO关键词
     */
    private String seoKeywords;

    /**
     * SEO描述
     */
    private String seoDescription;
} 