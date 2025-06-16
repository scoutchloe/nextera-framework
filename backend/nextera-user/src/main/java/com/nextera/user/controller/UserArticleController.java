package com.nextera.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextera.api.article.dto.ArticleCreateRequest;
import com.nextera.api.article.dto.ArticleDTO;
import com.nextera.common.core.Result;
import com.nextera.user.client.ArticleServiceClient;
import com.nextera.user.client.UserServiceClient;
import com.nextera.api.user.dto.UserDTO;
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

    private final ArticleServiceClient articleServiceClient;
    private final UserServiceClient userServiceClient;

    /**
     * 用户创建文章
     * 通过Dubbo RPC调用文章服务，同时记录用户操作
     */
    @PostMapping("/create")
    @Operation(summary = "用户创建文章", description = "用户通过RPC调用文章服务创建文章，同时记录用户操作")
    public Result<ArticleDTO> createArticle(
            @Valid @RequestBody ArticleCreateRequest request,
            @RequestParam @Parameter(description = "用户ID") Long userId,
            HttpServletRequest httpRequest) {
        
        log.info("用户创建文章请求，用户ID: {}, 文章标题: {}", userId, request.getTitle());
        
        try {
            // 1. 验证用户是否存在
            Result<UserDTO> userResult = userServiceClient.getUserById(userId);
            if (!userResult.isSuccess() || userResult.getData() == null) {
                return Result.error("用户不存在");
            }
            
            UserDTO user = userResult.getData();
            
            // 2. 更新用户最后活动时间
            userServiceClient.updateLastLoginTime(userId);
            
            // 3. 获取请求信息
            String ipAddress = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            // 4. 通过Dubbo RPC调用文章服务创建文章
            Result<ArticleDTO> result = articleServiceClient.createArticle(
                request, userId, user.getUsername(), ipAddress, userAgent);
            
            if (result.isSuccess()) {
                log.info("用户 {} 成功创建文章: {}", user.getUsername(), result.getData().getTitle());
            } else {
                log.error("用户 {} 创建文章失败: {}", user.getUsername(), result.getMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("用户创建文章异常", e);
            return Result.error("创建文章失败：" + e.getMessage());
        }
    }

    /**
     * 用户更新文章
     */
    @PutMapping("/update/{articleId}")
    @Operation(summary = "用户更新文章", description = "用户通过RPC调用文章服务更新文章")
    public Result<ArticleDTO> updateArticle(
            @PathVariable @Parameter(description = "文章ID") Long articleId,
            @Valid @RequestBody ArticleCreateRequest request,
            @RequestParam @Parameter(description = "用户ID") Long userId,
            HttpServletRequest httpRequest) {
        
        log.info("用户更新文章请求，用户ID: {}, 文章ID: {}", userId, articleId);
        
        try {
            // 1. 验证用户是否存在
            Result<UserDTO> userResult = userServiceClient.getUserById(userId);
            if (!userResult.isSuccess() || userResult.getData() == null) {
                return Result.error("用户不存在");
            }
            
            UserDTO user = userResult.getData();
            
            // 2. 检查用户是否有权限操作该文章
            Result<Boolean> permissionResult = articleServiceClient.checkUserArticlePermission(userId, articleId);
            if (!permissionResult.isSuccess() || !permissionResult.getData()) {
                return Result.error("无权限操作该文章");
            }
            
            // 3. 获取请求信息
            String ipAddress = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            // 4. 通过Dubbo RPC调用文章服务更新文章
            Result<ArticleDTO> result = articleServiceClient.updateArticle(
                articleId, request, userId, user.getUsername(), ipAddress, userAgent);
            
            if (result.isSuccess()) {
                log.info("用户 {} 成功更新文章: {}", user.getUsername(), result.getData().getTitle());
            } else {
                log.error("用户 {} 更新文章失败: {}", user.getUsername(), result.getMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("用户更新文章异常", e);
            return Result.error("更新文章失败：" + e.getMessage());
        }
    }

    /**
     * 用户删除文章
     */
    @DeleteMapping("/delete/{articleId}")
    @Operation(summary = "用户删除文章", description = "用户通过RPC调用文章服务删除文章")
    public Result<Boolean> deleteArticle(
            @PathVariable @Parameter(description = "文章ID") Long articleId,
            @RequestParam @Parameter(description = "用户ID") Long userId,
            HttpServletRequest httpRequest) {
        
        log.info("用户删除文章请求，用户ID: {}, 文章ID: {}", userId, articleId);
        
        try {
            // 1. 验证用户是否存在
            Result<UserDTO> userResult = userServiceClient.getUserById(userId);
            if (!userResult.isSuccess() || userResult.getData() == null) {
                return Result.error("用户不存在");
            }
            
            UserDTO user = userResult.getData();
            
            // 2. 检查用户是否有权限操作该文章
            Result<Boolean> permissionResult = articleServiceClient.checkUserArticlePermission(userId, articleId);
            if (!permissionResult.isSuccess() || !permissionResult.getData()) {
                return Result.error("无权限操作该文章");
            }
            
            // 3. 获取请求信息
            String ipAddress = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            // 4. 通过Dubbo RPC调用文章服务删除文章
            Result<Boolean> result = articleServiceClient.deleteArticle(
                articleId, userId, user.getUsername(), ipAddress, userAgent);
            
            if (result.isSuccess()) {
                log.info("用户 {} 成功删除文章ID: {}", user.getUsername(), articleId);
            } else {
                log.error("用户 {} 删除文章失败: {}", user.getUsername(), result.getMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("用户删除文章异常", e);
            return Result.error("删除文章失败：" + e.getMessage());
        }
    }

    /**
     * 用户发布文章
     */
    @PostMapping("/publish/{articleId}")
    @Operation(summary = "用户发布文章", description = "用户通过RPC调用文章服务发布文章")
    public Result<Boolean> publishArticle(
            @PathVariable @Parameter(description = "文章ID") Long articleId,
            @RequestParam @Parameter(description = "用户ID") Long userId,
            HttpServletRequest httpRequest) {
        
        log.info("用户发布文章请求，用户ID: {}, 文章ID: {}", userId, articleId);
        
        try {
            // 1. 验证用户是否存在
            Result<UserDTO> userResult = userServiceClient.getUserById(userId);
            if (!userResult.isSuccess() || userResult.getData() == null) {
                return Result.error("用户不存在");
            }
            
            UserDTO user = userResult.getData();
            
            // 2. 检查用户是否有权限操作该文章
            Result<Boolean> permissionResult = articleServiceClient.checkUserArticlePermission(userId, articleId);
            if (!permissionResult.isSuccess() || !permissionResult.getData()) {
                return Result.error("无权限操作该文章");
            }
            
            // 3. 获取请求信息
            String ipAddress = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            // 4. 通过Dubbo RPC调用文章服务发布文章
            Result<Boolean> result = articleServiceClient.publishArticle(
                articleId, userId, user.getUsername(), ipAddress, userAgent);
            
            if (result.isSuccess()) {
                log.info("用户 {} 成功发布文章ID: {}", user.getUsername(), articleId);
            } else {
                log.error("用户 {} 发布文章失败: {}", user.getUsername(), result.getMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("用户发布文章异常", e);
            return Result.error("发布文章失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户的文章列表
     */
    @GetMapping("/my-articles")
    @Operation(summary = "获取用户文章列表", description = "通过RPC调用文章服务获取用户的文章列表")
    public Result<Page<ArticleDTO>> getUserArticles(
            @RequestParam @Parameter(description = "用户ID") Long userId,
            @RequestParam(defaultValue = "1") @Parameter(description = "当前页") Integer page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页大小") Integer size) {
        
        log.info("获取用户文章列表，用户ID: {}, 页码: {}, 大小: {}", userId, page, size);
        
        try {
            // 1. 验证用户是否存在
            Result<UserDTO> userResult = userServiceClient.getUserById(userId);
            if (!userResult.isSuccess() || userResult.getData() == null) {
                return Result.error("用户不存在");
            }
            
            // 2. 通过Dubbo RPC调用文章服务获取用户文章列表
            Result<Page<ArticleDTO>> result = articleServiceClient.getArticlesByUserId(userId, page, size);
            
            if (result.isSuccess()) {
                log.info("成功获取用户 {} 的文章列表，共 {} 篇", userId, result.getData().getTotal());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("获取用户文章列表异常", e);
            return Result.error("获取文章列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户文章数量统计
     */
    @GetMapping("/count")
    @Operation(summary = "获取用户文章数量", description = "通过RPC调用文章服务获取用户的文章数量")
    public Result<Long> getUserArticleCount(@RequestParam @Parameter(description = "用户ID") Long userId) {
        
        log.info("获取用户文章数量，用户ID: {}", userId);
        
        try {
            // 1. 验证用户是否存在
            Result<UserDTO> userResult = userServiceClient.getUserById(userId);
            if (!userResult.isSuccess() || userResult.getData() == null) {
                return Result.error("用户不存在");
            }
            
            // 2. 通过Dubbo RPC调用文章服务获取用户文章数量
            Result<Long> result = articleServiceClient.getArticleCountByUserId(userId);
            
            if (result.isSuccess()) {
                log.info("用户 {} 共有 {} 篇文章", userId, result.getData());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("获取用户文章数量异常", e);
            return Result.error("获取文章数量失败：" + e.getMessage());
        }
    }

    /**
     * 获取文章详情
     */
    @GetMapping("/detail/{articleId}")
    @Operation(summary = "获取文章详情", description = "通过RPC调用文章服务获取文章详情")
    public Result<ArticleDTO> getArticleDetail(@PathVariable @Parameter(description = "文章ID") Long articleId) {
        
        log.info("获取文章详情，文章ID: {}", articleId);
        
        try {
            // 通过Dubbo RPC调用文章服务获取文章详情
            Result<ArticleDTO> result = articleServiceClient.getArticleById(articleId);
            
            if (result.isSuccess()) {
                log.info("成功获取文章详情: {}", result.getData().getTitle());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("获取文章详情异常", e);
            return Result.error("获取文章详情失败：" + e.getMessage());
        }
    }

    /**
     * 获取文章列表（支持筛选）
     */
    @GetMapping("/list")
    @Operation(summary = "获取文章列表", description = "通过RPC调用文章服务获取文章列表")
    public Result<Page<ArticleDTO>> getArticleList(
            @RequestParam(defaultValue = "1") @Parameter(description = "当前页") Integer page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页大小") Integer size,
            @RequestParam(required = false) @Parameter(description = "文章状态") Integer status,
            @RequestParam(required = false) @Parameter(description = "分类ID") Long categoryId,
            @RequestParam(required = false) @Parameter(description = "作者ID") Long authorId) {
        
        log.info("获取文章列表，页码: {}, 大小: {}, 状态: {}, 分类: {}, 作者: {}", 
                page, size, status, categoryId, authorId);
        
        try {
            // 通过Dubbo RPC调用文章服务获取文章列表
            Result<Page<ArticleDTO>> result = articleServiceClient.getArticleList(page, size, status, categoryId, authorId);
            
            if (result.isSuccess()) {
                log.info("成功获取文章列表，共 {} 篇", result.getData().getTotal());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("获取文章列表异常", e);
            return Result.error("获取文章列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
} 