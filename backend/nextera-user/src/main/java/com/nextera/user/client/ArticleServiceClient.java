package com.nextera.user.client;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextera.api.article.dto.ArticleCreateRequest;
import com.nextera.api.article.dto.ArticleDTO;
import com.nextera.api.article.service.ArticleService;
import com.nextera.common.core.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/**
 * 文章服务客户端
 * 提供用户服务调用文章服务的统一入口
 *
 * @author nextera
 * @since 2025-06-16
 */
@Slf4j
@Component
public class ArticleServiceClient {

    @DubboReference(version = "1.0.0", check = false, timeout = 5000)
    private ArticleService articleService;

    /**
     * 创建文章
     *
     * @param request 文章创建请求
     * @param userId 用户ID
     * @param username 用户名
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 创建的文章信息
     */
    public Result<ArticleDTO> createArticle(ArticleCreateRequest request, Long userId, String username, String ipAddress, String userAgent) {
        try {
            return articleService.createArticle(request, userId, username, ipAddress, userAgent);
        } catch (Exception e) {
            log.error("调用文章服务创建文章失败", e);
            return Result.error("创建文章失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取文章详情
     *
     * @param id 文章ID
     * @return 文章详情
     */
    public Result<ArticleDTO> getArticleById(Long id) {
        try {
            return articleService.getArticleById(id);
        } catch (Exception e) {
            log.error("调用文章服务获取文章详情失败", e);
            return Result.error("获取文章详情失败：" + e.getMessage());
        }
    }

    /**
     * 更新文章
     *
     * @param id 文章ID
     * @param request 文章更新请求
     * @param userId 用户ID
     * @param username 用户名
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 更新后的文章信息
     */
    public Result<ArticleDTO> updateArticle(Long id, ArticleCreateRequest request, Long userId, String username, String ipAddress, String userAgent) {
        try {
            return articleService.updateArticle(id, request, userId, username, ipAddress, userAgent);
        } catch (Exception e) {
            log.error("调用文章服务更新文章失败", e);
            return Result.error("更新文章失败：" + e.getMessage());
        }
    }

    /**
     * 删除文章
     *
     * @param id 文章ID
     * @param userId 用户ID
     * @param username 用户名
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 删除结果
     */
    public Result<Boolean> deleteArticle(Long id, Long userId, String username, String ipAddress, String userAgent) {
        try {
            return articleService.deleteArticle(id, userId, username, ipAddress, userAgent);
        } catch (Exception e) {
            log.error("调用文章服务删除文章失败", e);
            return Result.error("删除文章失败：" + e.getMessage());
        }
    }

    /**
     * 发布文章
     *
     * @param id 文章ID
     * @param userId 用户ID
     * @param username 用户名
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 发布结果
     */
    public Result<Boolean> publishArticle(Long id, Long userId, String username, String ipAddress, String userAgent) {
        try {
            return articleService.publishArticle(id, userId, username, ipAddress, userAgent);
        } catch (Exception e) {
            log.error("调用文章服务发布文章失败", e);
            return Result.error("发布文章失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询文章列表
     *
     * @param page 当前页
     * @param size 每页大小
     * @param status 文章状态
     * @param categoryId 分类ID
     * @param authorId 作者ID
     * @return 文章分页列表
     */
    public Result<Page<ArticleDTO>> getArticleList(Integer page, Integer size, Integer status, Long categoryId, Long authorId) {
        try {
            return articleService.getArticleList(page, size, status, categoryId, authorId);
        } catch (Exception e) {
            log.error("调用文章服务获取文章列表失败", e);
            return Result.error("获取文章列表失败：" + e.getMessage());
        }
    }

    /**
     * 根据用户ID获取文章数量
     *
     * @param userId 用户ID
     * @return 文章数量
     */
    public Result<Long> getArticleCountByUserId(Long userId) {
        try {
            return articleService.getArticleCountByUserId(userId);
        } catch (Exception e) {
            log.error("调用文章服务获取用户文章数量失败", e);
            return Result.error("获取用户文章数量失败：" + e.getMessage());
        }
    }

    /**
     * 根据用户ID获取用户的文章列表
     *
     * @param userId 用户ID
     * @param page 当前页
     * @param size 每页大小
     * @return 用户文章列表
     */
    public Result<Page<ArticleDTO>> getArticlesByUserId(Long userId, Integer page, Integer size) {
        try {
            return articleService.getArticlesByUserId(userId, page, size);
        } catch (Exception e) {
            log.error("调用文章服务获取用户文章列表失败", e);
            return Result.error("获取用户文章列表失败：" + e.getMessage());
        }
    }

    /**
     * 检查用户是否有文章操作权限
     *
     * @param userId 用户ID
     * @param articleId 文章ID
     * @return 是否有权限
     */
    public Result<Boolean> checkUserArticlePermission(Long userId, Long articleId) {
        try {
            Result<ArticleDTO> articleResult = articleService.getArticleById(articleId);
            if (!articleResult.isSuccess() || articleResult.getData() == null) {
                return Result.error("文章不存在");
            }
            
            ArticleDTO article = articleResult.getData();
            boolean hasPermission = userId.equals(article.getAuthorId());
            
            return Result.success(hasPermission);
        } catch (Exception e) {
            log.error("检查用户文章权限失败", e);
            return Result.error("检查权限失败：" + e.getMessage());
        }
    }
} 