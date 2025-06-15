package com.nextera.article.dubbo;

import com.nextera.article.dto.ArticleCreateRequest;
import com.nextera.article.dto.ArticleDTO;
import com.nextera.common.core.Result;

/**
 * 文章服务Dubbo接口
 *
 * @author nextera
 * @since 2025-06-16
 */
public interface ArticleService {

    /**
     * 创建文章（用户写文章同时记录用户操作记录）
     *
     * @param request 文章创建请求
     * @param userId 用户ID
     * @param username 用户名
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 创建结果
     */
    Result<ArticleDTO> createArticle(ArticleCreateRequest request, Long userId, String username, String ipAddress, String userAgent);

    /**
     * 根据ID获取文章信息
     *
     * @param id 文章ID
     * @return 文章信息
     */
    Result<ArticleDTO> getArticleById(Long id);

    /**
     * 更新文章
     *
     * @param id 文章ID
     * @param request 更新请求
     * @param userId 用户ID
     * @param username 用户名
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     * @return 更新结果
     */
    Result<ArticleDTO> updateArticle(Long id, ArticleCreateRequest request, Long userId, String username, String ipAddress, String userAgent);

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
    Result<Boolean> deleteArticle(Long id, Long userId, String username, String ipAddress, String userAgent);

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
    Result<Boolean> publishArticle(Long id, Long userId, String username, String ipAddress, String userAgent);
} 