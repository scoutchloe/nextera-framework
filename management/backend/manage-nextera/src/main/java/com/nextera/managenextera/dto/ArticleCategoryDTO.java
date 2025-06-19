package com.nextera.managenextera.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章分类DTO
 *
 * @author nextera
 * @since 2025-06-19
 */
@Data
@Schema(description = "文章分类DTO")
public class ArticleCategoryDTO {

    @Schema(description = "分类ID")
    private Long id;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "分类描述")
    private String description;

    @Schema(description = "父分类ID")
    private Long parentId;

    @Schema(description = "父分类名称")
    private String parentName;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "分类状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "分类图标")
    private String icon;

    @Schema(description = "分类颜色")
    private String color;

    @Schema(description = "文章数量")
    private Long articleCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "创建者ID")
    private Long createBy;

    @Schema(description = "更新者ID")
    private Long updateBy;

    @Schema(description = "子分类列表")
    private List<ArticleCategoryDTO> children;
} 