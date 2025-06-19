package com.nextera.managenextera.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextera.managenextera.common.Result;
import com.nextera.managenextera.dto.ArticleCategoryDTO;
import com.nextera.managenextera.entity.articlemod.ArticleCategory;
import com.nextera.managenextera.service.ArticleCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 文章分类管理Controller
 *
 * @author nextera
 * @since 2025-06-19
 */
@Tag(name = "文章分类管理", description = "文章分类管理相关接口")
@RestController
@RequestMapping("/article/category")
@Slf4j
public class ArticleCategoryController {

    @Autowired
    private ArticleCategoryService categoryService;

    @Operation(summary = "分页查询分类", description = "分页查询文章分类列表")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('article:category:list')")
    public Result<IPage<ArticleCategoryDTO>> getCategoryPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false, name = "name") String name,
            @RequestParam(required = false, name = "status") Integer status) {

        Page<ArticleCategory> page = new Page<>(current, size);
        IPage<ArticleCategoryDTO> result = categoryService.getCategoryPage(page, name, status);

        return Result.success(result);
    }

    @Operation(summary = "获取分类树", description = "获取文章分类树形结构")
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('article:category:list')")
    public Result<List<ArticleCategoryDTO>> getCategoryTree() {
        List<ArticleCategoryDTO> result = categoryService.getCategoryTree();
        return Result.success(result);
    }

    @Operation(summary = "获取启用的分类", description = "获取所有启用状态的分类")
    @GetMapping("/enabled")
    @PreAuthorize("hasAuthority('article:category:list')")
    public Result<List<ArticleCategoryDTO>> getEnabledCategories() {
        List<ArticleCategoryDTO> result = categoryService.getEnabledCategories();
        return Result.success(result);
    }

    @Operation(summary = "根据ID获取分类详情", description = "根据分类ID获取分类详细信息")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('article:category:list')")
    public Result<ArticleCategoryDTO> getCategoryById(
            @Parameter(description = "分类ID") @PathVariable Long id) {

        ArticleCategoryDTO category = categoryService.getCategoryById(id);
        if (category != null) {
            return Result.success(category);
        } else {
            return Result.error("分类不存在");
        }
    }

    @Operation(summary = "创建分类", description = "创建新的文章分类")
    @PostMapping
    @PreAuthorize("hasAuthority('article:category:add')")
    public Result<Boolean> createCategory(@Validated @RequestBody ArticleCategoryDTO categoryDTO) {

        boolean success = categoryService.createCategory(categoryDTO);
        return Result.success(success);

    }

    @Operation(summary = "更新分类", description = "更新文章分类信息")
    @PutMapping
    @PreAuthorize("hasAuthority('article:category:edit')")
    public Result<Boolean> updateCategory(@Validated @RequestBody ArticleCategoryDTO categoryDTO) {

        boolean success = categoryService.updateCategory(categoryDTO);
        return Result.success("更新成功", true);
    }

    @Operation(summary = "删除分类", description = "根据ID删除文章分类")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('article:category:delete')")
    public Result<Boolean> deleteCategory(
            @Parameter(description = "分类ID") @PathVariable(name = "id") Long id) {

        boolean success = categoryService.deleteCategory(id);
        return Result.success(success);

    }

    @Operation(summary = "批量删除分类", description = "批量删除文章分类")
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('article:category:delete')")
    public Result<Boolean> deleteCategories(@RequestBody List<Long> ids) {

        boolean success = categoryService.deleteCategories(ids);
        return Result.success("批量删除成功", success);

    }

    @Operation(summary = "更新分类状态", description = "更新文章分类状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('article:category:edit')")
    public Result<Boolean> updateCategoryStatus(
            @Parameter(description = "分类ID") @PathVariable(name = "id") Long id,
            @RequestParam(name = "status") Integer status) {

        boolean success = categoryService.updateCategoryStatus(id, status);
        return Result.success(status == 1 ? "启用成功" : "禁用成功", success);

    }
} 