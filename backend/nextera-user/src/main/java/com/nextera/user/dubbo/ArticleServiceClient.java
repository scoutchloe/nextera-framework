package com.nextera.user.dubbo;

import com.nextera.common.core.Result;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/**
 * 文章服务Dubbo客户端
 *
 * @author nextera
 * @since 2025-06-16
 */
@Component
public class ArticleServiceClient {

    /**
     * 引用远程文章服务
     */
    @DubboReference
    private com.nextera.article.dubbo.ArticleService articleService;

    /**
     * 创建文章并记录用户操作
     *
     * @param request 文章创建请求
     * @param userId 用户ID
     * @param username 用户名
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 创建结果
     */
    public Result<?> createArticleWithUserRecord(Object request, Long userId, String username, String ipAddress, String userAgent) {
        try {
            // 这里需要将request转换为ArticleCreateRequest
            // 由于跨模块依赖，这里使用Object类型，在实际使用时需要处理类型转换
            return articleService.createArticle((com.nextera.article.dto.ArticleCreateRequest) request, userId, username, ipAddress, userAgent);
        } catch (Exception e) {
            return Result.error("调用文章服务失败: " + e.getMessage());
        }
    }

    /**
     * 获取文章信息
     *
     * @param id 文章ID
     * @return 文章信息
     */
    public Result<?> getArticleById(Long id) {
        try {
            return articleService.getArticleById(id);
        } catch (Exception e) {
            return Result.error("调用文章服务失败: " + e.getMessage());
        }
    }

    /**
     * 更新文章并记录用户操作
     *
     * @param id 文章ID
     * @param request 更新请求
     * @param userId 用户ID
     * @param username 用户名
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 更新结果
     */
    public Result<?> updateArticleWithUserRecord(Long id, Object request, Long userId, String username, String ipAddress, String userAgent) {
        try {
            return articleService.updateArticle(id, (com.nextera.article.dto.ArticleCreateRequest) request, userId, username, ipAddress, userAgent);
        } catch (Exception e) {
            return Result.error("调用文章服务失败: " + e.getMessage());
        }
    }

    /**
     * 删除文章并记录用户操作
     *
     * @param id 文章ID
     * @param userId 用户ID
     * @param username 用户名
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 删除结果
     */
    public Result<Boolean> deleteArticleWithUserRecord(Long id, Long userId, String username, String ipAddress, String userAgent) {
        try {
            return articleService.deleteArticle(id, userId, username, ipAddress, userAgent);
        } catch (Exception e) {
            return Result.error("调用文章服务失败: " + e.getMessage());
        }
    }

    /**
     * 发布文章并记录用户操作
     *
     * @param id 文章ID
     * @param userId 用户ID
     * @param username 用户名
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 发布结果
     */
    public Result<Boolean> publishArticleWithUserRecord(Long id, Long userId, String username, String ipAddress, String userAgent) {
        try {
            return articleService.publishArticle(id, userId, username, ipAddress, userAgent);
        } catch (Exception e) {
            return Result.error("调用文章服务失败: " + e.getMessage());
        }
    }
} 