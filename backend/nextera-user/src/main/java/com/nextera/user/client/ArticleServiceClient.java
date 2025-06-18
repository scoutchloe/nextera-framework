package com.nextera.user.client;

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
     * 专门用于TCC阶段的文章服务引用
     * 配置跳过Seata事务传播过滤器，避免TCC阶段的Dubbo-Seata集成问题
     */
    @DubboReference(
        version = "1.0.0", 
        check = false, 
        timeout = 5000,
        filter = "-seataTransactionPropagation"  // 跳过Seata事务传播过滤器
    )
    private ArticleService articleServiceForTcc;

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
    public Result<Integer> createArticle(ArticleCreateRequest request, Long userId, String username, String ipAddress, String userAgent) {
        try {
            return articleService.createArticle(request, userId, username, ipAddress, userAgent);
        } catch (Exception e) {
            log.error("调用文章服务创建文章失败", e);
            return Result.error("创建文章失败：" + e.getMessage());
        }
    }

    /**
     * 更新文章（普通调用）
     *
     * @param articleId 文章ID
     * @param request 文章更新请求
     * @param userId 用户ID
     * @param username 用户名
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 更新结果
     */
    public Result<Boolean> updateArticle(Long articleId, ArticleCreateRequest request, Long userId, String username, String ipAddress, String userAgent) {
        try {
            return articleService.updateArticle(articleId, request, userId, username, ipAddress, userAgent);
        } catch (Exception e) {
            log.error("调用文章服务更新文章失败", e);
            return Result.error("更新文章失败：" + e.getMessage());
        }
    }

    /**
     * 更新文章（TCC专用）
     * 使用配置了跳过Seata事务传播的Dubbo引用，避免TCC阶段的集成问题
     *
     * @param articleId 文章ID
     * @param request 文章更新请求
     * @param userId 用户ID
     * @param username 用户名
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 更新结果
     */
    public Result<Boolean> updateArticleForTcc(Long articleId, ArticleCreateRequest request, Long userId, String username, String ipAddress, String userAgent) {
        try {
            log.info("TCC专用方法：调用文章服务更新文章，articleId={}", articleId);
            Result<Boolean> result = articleServiceForTcc.updateArticle(articleId, request, userId, username, ipAddress, userAgent);
            log.info("TCC专用方法：文章服务调用结果，articleId={}, success={}", 
                articleId, result.isSuccess());
            return result;
        } catch (Exception e) {
            log.error("TCC专用方法：调用文章服务更新文章失败，articleId={}", articleId, e);
            return Result.error("更新文章失败：" + e.getMessage());
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
     * 根据ID获取文章详情（TCC专用）
     * 使用配置了跳过Seata事务传播的Dubbo引用
     *
     * @param id 文章ID
     * @return 文章详情
     */
    public Result<ArticleDTO> getArticleByIdForTcc(Long id) {
        try {
            log.debug("TCC专用方法：调用文章服务获取文章详情，articleId={}", id);
            return articleServiceForTcc.getArticleById(id);
        } catch (Exception e) {
            log.error("TCC专用方法：调用文章服务获取文章详情失败，articleId={}", id, e);
            return Result.error("获取文章详情失败：" + e.getMessage());
        }
    }
} 