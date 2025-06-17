package com.nextera.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextera.api.article.dto.ArticleCreateRequest;
import com.nextera.api.article.dto.ArticleDTO;
import com.nextera.common.core.Result;
import com.nextera.user.service.UserArticleBizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 用户文章控制器
 * 通过Dubbo RPC调用文章服务，实现用户写文章功能
 * 写文章的同时记录用户操作记录
 *
 * @author nextera
 * @since 2025-06-16
 */
@Slf4j
@RestController
@RequestMapping("/api/user/article")
@RequiredArgsConstructor
@Validated
@Tag(name = "用户文章管理", description = "用户通过RPC调用文章服务的相关接口")
public class UserArticleController {

    private final UserArticleBizService userArticleBizService;

    /**
     * 用户创建文章
     * 通过Dubbo RPC调用文章服务，同时记录用户操作
     */
    @PostMapping("/create")
    @Operation(summary = "用户创建文章", description = "用户通过RPC调用文章服务创建文章，同时记录用户操作")
    public Result<Integer> createArticle(
            @Valid @RequestBody ArticleCreateRequest request,
            @Parameter(description = "用户ID")
            @RequestParam(name="userId")  Long userId,
//            @RequestParam Long userId,
            HttpServletRequest httpRequest) {
        
        log.info("接收用户创建文章请求，用户ID: {}, 文章标题: {}", userId, request.getTitle());
        return userArticleBizService.createArticle(request, userId, httpRequest);
    }

} 