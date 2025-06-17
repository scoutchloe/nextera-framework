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
} 