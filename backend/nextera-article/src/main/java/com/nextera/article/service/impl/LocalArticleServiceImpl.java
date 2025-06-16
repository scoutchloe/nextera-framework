package com.nextera.article.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextera.article.dto.ArticleCreateRequest;
import com.nextera.article.dto.ArticleDTO;
import com.nextera.article.entity.Article;
import com.nextera.article.mapper.ArticleCategoryMapper;
import com.nextera.article.mapper.ArticleMapper;
import com.nextera.article.service.LocalArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 文章本地服务实现类
 *
 * @author nextera
 * @since 2025-06-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalArticleServiceImpl implements LocalArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleCategoryMapper categoryMapper;

    @Override
    public Integer createArticle(ArticleCreateRequest request) {
        return 0;
    }

    @Override
    public int createArticleInner(ArticleDTO articleDTO) {
        Article article = new Article();
        BeanUtils.copyProperties(articleDTO, article);
        return articleMapper.insert(article);
    }

    @Override
    public ArticleDTO getArticleById(Long id) {
        return null;
    }

    @Override
    public boolean updateArticle(Long id, ArticleCreateRequest request) {
        return false;
    }

    @Override
    public boolean deleteArticle(Long id) {
        return false;
    }

    @Override
    public boolean publishArticle(Long id) {
        return false;
    }

    @Override
    public Page<ArticleDTO> getArticleList(Integer page, Integer size, Integer status, Long categoryId, Long authorId) {
        return null;
    }
}