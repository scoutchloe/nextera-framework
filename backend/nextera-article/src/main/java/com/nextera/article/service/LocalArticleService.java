package com.nextera.article.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextera.article.dto.ArticleCreateRequest;
import com.nextera.article.dto.ArticleDTO;

/**
 * 文章本地服务接口
 *
 * @author nextera
 * @since 2025-06-16
 */
public interface LocalArticleService {

    /**
     * 创建文章
     *
     * @param request 文章创建请求
     * @return 创建结果
     */
    Integer createArticle(ArticleCreateRequest request);

    int createArticleInner(ArticleDTO articleDTO);

    /**
     * 根据ID获取文章信息
     *
     * @param id 文章ID
     * @return 文章信息
     */
    ArticleDTO getArticleById(Long id);

    /**
     * 更新文章
     *
     * @param id 文章ID
     * @param request 更新请求
     * @return 更新结果
     */
    boolean updateArticle(Long id, com.nextera.article.dto.ArticleCreateRequest request);

    boolean updateArticleInner(Long id, com.nextera.api.article.dto.ArticleCreateRequest request);

    /**
     * 删除文章
     *
     * @param id 文章ID
     * @return 删除结果
     */
    boolean deleteArticle(Long id);

    /**
     * 发布文章
     *
     * @param id 文章ID
     * @return 发布结果
     */
    boolean publishArticle(Long id);

    /**
     * 分页查询文章列表
     *
     * @param page 当前页
     * @param size 每页大小
     * @param status 文章状态
     * @param categoryId 分类ID
     * @param authorId 作者ID
     * @return 文章列表
     */
    Page<ArticleDTO> getArticleList(Integer page, Integer size, Integer status, Long categoryId, Long authorId);

}