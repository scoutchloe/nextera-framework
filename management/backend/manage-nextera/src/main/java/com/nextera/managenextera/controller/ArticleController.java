package com.nextera.managenextera.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextera.managenextera.common.Result;
import com.nextera.managenextera.dto.ArticleDTO;
import com.nextera.managenextera.entity.articlemod.Article;
import com.nextera.managenextera.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章管理Controller
 *
 * @author nextera
 * @since 2025-06-19
 */
@Tag(name = "文章管理", description = "文章管理相关接口")
@RestController
@RequestMapping("/article")
@Slf4j
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @Operation(summary = "分页查询文章", description = "分页查询文章列表")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('article:list')")
    public Result<IPage<ArticleDTO>> getArticlePage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String authorName) {

        Page<Article> page = new Page<>(current, size);
        IPage<ArticleDTO> result = articleService.getArticlePage(page, title, categoryId, status, authorName);

        return Result.success(result);
    }

    @Operation(summary = "根据ID获取文章详情", description = "根据文章ID获取文章详细信息")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('article:list')")
    public Result<ArticleDTO> getArticleById(
            @Parameter(description = "文章ID") @PathVariable Long id) {

        ArticleDTO article = articleService.getArticleById(id);
        if (article != null) {
            return Result.success(article);
        } else {
            return Result.error("文章不存在");
        }
    }

    @Operation(summary = "创建文章", description = "创建新文章")
    @PostMapping
    @PreAuthorize("hasAuthority('article:add')")
    public Result<Boolean> createArticle(@Validated @RequestBody ArticleDTO articleDTO) {
        boolean success = articleService.createArticle(articleDTO);
        return Result.success(success);
    }

    @Operation(summary = "更新文章", description = "更新文章信息")
    @PutMapping
    @PreAuthorize("hasAuthority('article:edit')")
    public Result<Boolean> updateArticle(@Validated @RequestBody ArticleDTO articleDTO) {
        boolean success = articleService.updateArticle(articleDTO);
        return Result.success(success);
    }

    @Operation(summary = "删除文章", description = "根据ID删除文章")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('article:delete')")
    public Result<Boolean> deleteArticle(
            @Parameter(description = "文章ID") @PathVariable Long id) {
        boolean success = articleService.deleteArticle(id);
        return Result.success(success);
    }

    @Operation(summary = "批量删除文章", description = "批量删除文章")
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('article:delete')")
    public Result<Boolean> deleteArticles(@RequestBody List<Long> ids) {
        boolean success = articleService.deleteArticles(ids);
        return Result.success(success);
    }

    @Operation(summary = "发布文章", description = "发布文章")
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('article:edit')")
    public Result<Boolean> publishArticle(
            @Parameter(description = "文章ID") @PathVariable Long id) {
        boolean success = articleService.publishArticle(id);
        return Result.success(success);
    }

    @Operation(summary = "下架文章", description = "下架文章")
    @PutMapping("/{id}/unpublish")
    @PreAuthorize("hasAuthority('article:edit')")
    public Result<Boolean> unpublishArticle(
            @Parameter(description = "文章ID") @PathVariable Long id) {
        boolean success = articleService.unpublishArticle(id);
        return Result.success(success);
    }

    @Operation(summary = "设置文章置顶", description = "设置文章置顶状态")
    @PutMapping("/{id}/top")
    @PreAuthorize("hasAuthority('article:edit')")
    public Result<Boolean> setArticleTop(
            @Parameter(description = "文章ID") @PathVariable Long id,
            @RequestParam Integer isTop) {
        boolean success = articleService.setArticleTop(id, isTop);
        return Result.success(success);
    }

    @Operation(summary = "设置文章推荐", description = "设置文章推荐状态")
    @PutMapping("/{id}/recommend")
    @PreAuthorize("hasAuthority('article:edit')")
    public Result<Boolean> setArticleRecommend(
            @Parameter(description = "文章ID") @PathVariable Long id,
            @RequestParam Integer isRecommend) {
        boolean success = articleService.setArticleRecommend(id, isRecommend);
        return Result.success(success);
    }
} 