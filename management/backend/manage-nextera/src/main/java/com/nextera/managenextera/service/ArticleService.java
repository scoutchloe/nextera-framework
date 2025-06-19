package com.nextera.managenextera.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nextera.managenextera.dto.ArticleDTO;
import com.nextera.managenextera.entity.articlemod.Article;

import java.util.List;

/**
 * 文章服务接口
 *
 * @author nextera
 * @since 2025-06-19
 */
public interface ArticleService extends IService<Article> {

    /**
     * 分页查询文章列表
     *
     * @param page 分页参数
     * @param title 标题（可选）
     * @param categoryId 分类ID（可选）
     * @param status 状态（可选）
     * @param authorName 作者名称（可选）
     * @return 分页结果
     */
    IPage<ArticleDTO> getArticlePage(Page<Article> page, String title, Long categoryId, 
                                     Integer status, String authorName);

    /**
     * 根据ID获取文章详情
     *
     * @param id 文章ID
     * @return 文章DTO
     */
    ArticleDTO getArticleById(Long id);

    /**
     * 创建文章
     *
     * @param articleDTO 文章DTO
     * @return 是否成功
     */
    boolean createArticle(ArticleDTO articleDTO);

    /**
     * 更新文章
     *
     * @param articleDTO 文章DTO
     * @return 是否成功
     */
    boolean updateArticle(ArticleDTO articleDTO);

    /**
     * 删除文章
     *
     * @param id 文章ID
     * @return 是否成功
     */
    boolean deleteArticle(Long id);

    /**
     * 批量删除文章
     *
     * @param ids 文章ID列表
     * @return 是否成功
     */
    boolean deleteArticles(List<Long> ids);

    /**
     * 发布文章
     *
     * @param id 文章ID
     * @return 是否成功
     */
    boolean publishArticle(Long id);

    /**
     * 下架文章
     *
     * @param id 文章ID
     * @return 是否成功
     */
    boolean unpublishArticle(Long id);

    /**
     * 设置文章置顶
     *
     * @param id 文章ID
     * @param isTop 是否置顶
     * @return 是否成功
     */
    boolean setArticleTop(Long id, Integer isTop);

    /**
     * 设置文章推荐
     *
     * @param id 文章ID
     * @param isRecommend 是否推荐
     * @return 是否成功
     */
    boolean setArticleRecommend(Long id, Integer isRecommend);
} 