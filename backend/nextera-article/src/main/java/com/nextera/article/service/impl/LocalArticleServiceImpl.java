package com.nextera.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
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

import java.time.LocalDateTime;

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

        // 设置默认值
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCommentCount(0L);
        article.setIsTop(0);
        article.setIsRecommend(0);
        article.setStatus(0); // 默认草稿状态
        article.setIsDeleted(0);

        int result = articleMapper.insert(article);
        log.info("成功创建文章，文章ID: {}, 标题: {}", article.getId(), article.getTitle());
        return result;
    }

    @Override
    public ArticleDTO getArticleById(Long id) {
        Article article = articleMapper.selectById(id);
        ArticleDTO articleDTO = new ArticleDTO();
        BeanUtils.copyProperties(article, articleDTO);
        return articleDTO;
    }

    @Override
    public boolean updateArticleInner(Long id, com.nextera.api.article.dto.ArticleCreateRequest request) {
        LambdaUpdateWrapper<Article> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Article::getId, id);
        updateWrapper.set(Article::getTitle, request.getTitle());
        updateWrapper.set(Article::getContent, request.getContent());
        updateWrapper.set(Article::getCategoryId, request.getCategoryId());
        updateWrapper.set(Article::getUpdateTime, LocalDateTime.now());
        LambdaUpdateChainWrapper<Article> updateChainWrapper = new LambdaUpdateChainWrapper<>(articleMapper, updateWrapper);
        return updateChainWrapper.update();
    }

    @Override
    public boolean updateArticle(Long id, ArticleCreateRequest request) {
        
        LambdaUpdateWrapper<Article> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Article::getId, id);
        updateWrapper.set(Article::getTitle, request.getTitle());
        updateWrapper.set(Article::getContent, request.getContent());
        updateWrapper.set(Article::getCategoryId, request.getCategoryId());
        updateWrapper.set(Article::getIsTop, -1);
        LambdaUpdateChainWrapper<Article> updateChainWrapper = new LambdaUpdateChainWrapper<>(articleMapper, updateWrapper);
        boolean result = updateChainWrapper.update();
        
        log.info("文章更新成功，文章ID: {}, 标题: {}, 结果: {}", id, request.getTitle(), result);
        return result;
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