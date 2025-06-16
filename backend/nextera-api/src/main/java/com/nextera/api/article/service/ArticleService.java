package com.nextera.api.article.service;

import com.nextera.api.article.dto.ArticleCreateRequest;
import com.nextera.api.article.dto.ArticleDTO;
import com.nextera.common.core.Result;

/**
 * 文章服务Dubbo接口
 *
 * @author nextera
 * @since 2025-06-16
 */
public interface ArticleService {

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
    Result<Integer> createArticle(ArticleCreateRequest request, Long userId, String username, String ipAddress, String userAgent);

    /**
     * 根据ID获取文章详情
     *
     * @param id 文章ID
     * @return 文章详情
     */
    Result<ArticleDTO> getArticleById(Long id);

} 