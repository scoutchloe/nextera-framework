package com.nextera.api.article.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
    Result<ArticleDTO> createArticle(ArticleCreateRequest request, Long userId, String username, String ipAddress, String userAgent);

    /**
     * 根据ID获取文章详情
     *
     * @param id 文章ID
     * @return 文章详情
     */
    Result<ArticleDTO> getArticleById(Long id);

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
    Result<Page<ArticleDTO>> getArticleList(Integer page, Integer size, Integer status, Long categoryId, Long authorId);

    /**
     * 根据用户ID获取文章数量
     *
     * @param userId 用户ID
     * @return 文章数量
     */
    Result<Long> getArticleCountByUserId(Long userId);

    /**
     * 根据用户ID获取用户的文章列表
     *
     * @param userId 用户ID
     * @param page 当前页
     * @param size 每页大小
     * @return 用户文章列表
     */
    Result<Page<ArticleDTO>> getArticlesByUserId(Long userId, Integer page, Integer size);
} 