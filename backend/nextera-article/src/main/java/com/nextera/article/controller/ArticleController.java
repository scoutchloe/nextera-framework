package com.nextera.article.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextera.article.dto.ArticleCreateRequest;
import com.nextera.article.dto.ArticleDTO;
import com.nextera.article.service.LocalArticleService;
import com.nextera.common.core.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 文章管理Controller
 *
 * @author nextera
 * @since 2025-06-16
 */
@Slf4j
@RestController
@RequestMapping("/article")
@RequiredArgsConstructor
@Tag(name = "文章管理", description = "文章管理相关接口")
public class ArticleController {

    private final LocalArticleService articleService;

    @PostMapping
    @Operation(summary = "创建文章", description = "创建新的文章")
    public Result<ArticleDTO> createArticle(@Validated @RequestBody ArticleCreateRequest request) {
        log.info("创建文章请求: {}", request);
        return articleService.createArticle(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取文章详情", description = "根据ID获取文章详细信息")
    public Result<ArticleDTO> getArticleById(@PathVariable @Parameter(description = "文章ID") Long id) {
        log.info("获取文章详情: {}", id);
        return articleService.getArticleById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新文章", description = "更新文章信息")
    public Result<ArticleDTO> updateArticle(
            @PathVariable @Parameter(description = "文章ID") Long id,
            @Validated @RequestBody ArticleCreateRequest request) {
        log.info("更新文章: {}, 请求: {}", id, request);
        return articleService.updateArticle(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除文章", description = "删除指定的文章")
    public Result<Boolean> deleteArticle(@PathVariable @Parameter(description = "文章ID") Long id) {
        log.info("删除文章: {}", id);
        return articleService.deleteArticle(id);
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "发布文章", description = "发布指定的文章")
    public Result<Boolean> publishArticle(@PathVariable @Parameter(description = "文章ID") Long id) {
        log.info("发布文章: {}", id);
        return articleService.publishArticle(id);
    }

    @GetMapping("/list")
    @Operation(summary = "获取文章列表", description = "分页查询文章列表")
    public Result<Page<ArticleDTO>> getArticleList(
            @RequestParam(defaultValue = "1") @Parameter(description = "当前页") Integer page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页大小") Integer size,
            @RequestParam(required = false) @Parameter(description = "文章状态") Integer status,
            @RequestParam(required = false) @Parameter(description = "分类ID") Long categoryId,
            @RequestParam(required = false) @Parameter(description = "作者ID") Long authorId) {
        log.info("查询文章列表: page={}, size={}, status={}, categoryId={}, authorId={}", 
                page, size, status, categoryId, authorId);
        return articleService.getArticleList(page, size, status, categoryId, authorId);
    }

    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查服务是否正常运行")
    public Result<String> health() {
        return Result.success("Article service is healthy");
    }
} 