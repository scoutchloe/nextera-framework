package com.nextera.managenextera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nextera.managenextera.dto.ArticleDTO;
import com.nextera.managenextera.entity.articlemod.Article;
import com.nextera.managenextera.entity.articlemod.ArticleCategory;
import com.nextera.managenextera.mapper.ArticleMapper;
import com.nextera.managenextera.service.ArticleCategoryService;
import com.nextera.managenextera.service.ArticleService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章服务实现类
 *
 * @author nextera
 * @since 2025-06-19
 */
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Autowired
    private ArticleCategoryService articleCategoryService;

    @Override
    public IPage<ArticleDTO> getArticlePage(Page<Article> page, String title, Long categoryId, 
                                            Integer status, String authorName) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(title)) {
            queryWrapper.like(Article::getTitle, title);
        }
        if (categoryId != null) {
            queryWrapper.eq(Article::getCategoryId, categoryId);
        }
        if (status != null) {
            queryWrapper.eq(Article::getStatus, status);
        }
        if (StringUtils.hasText(authorName)) {
            queryWrapper.like(Article::getAuthorName, authorName);
        }

        queryWrapper.orderByDesc(Article::getCreateTime);

        IPage<Article> articlePage = this.page(page, queryWrapper);
        
        // 转换为DTO
        IPage<ArticleDTO> result = articlePage.convert(this::convertToDTO);
        
        return result;
    }

    @Override
    public ArticleDTO getArticleById(Long id) {
        Article article = this.getById(id);
        if (article == null) {
            return null;
        }
        return convertToDTO(article);
    }

    @Override
    @Transactional
    public boolean createArticle(ArticleDTO articleDTO) {
        Article article = convertToEntity(articleDTO);
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        
        // 如果是发布状态，设置发布时间
        if (article.getStatus() == 1) {
            article.setPublishTime(LocalDateTime.now());
        }
        
        return this.save(article);
    }

    @Override
    @Transactional
    public boolean updateArticle(ArticleDTO articleDTO) {
        Article existingArticle = this.getById(articleDTO.getId());
        if (existingArticle == null) {
            throw new RuntimeException("文章不存在");
        }

        Article article = convertToEntity(articleDTO);
        article.setUpdateTime(LocalDateTime.now());
        
        // 如果状态从非发布改为发布，设置发布时间
        if (existingArticle.getStatus() != 1 && article.getStatus() == 1) {
            article.setPublishTime(LocalDateTime.now());
        }
        
        return this.updateById(article);
    }

    @Override
    @Transactional
    public boolean deleteArticle(Long id) {
        return this.removeById(id);
    }

    @Override
    @Transactional
    public boolean deleteArticles(List<Long> ids) {
        return this.removeByIds(ids);
    }

    @Override
    @Transactional
    public boolean publishArticle(Long id) {
        Article article = new Article();
        article.setId(id);
        article.setStatus(1);
        article.setPublishTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        return this.updateById(article);
    }

    @Override
    @Transactional
    public boolean unpublishArticle(Long id) {
        Article article = new Article();
        article.setId(id);
        article.setStatus(2);
        article.setUpdateTime(LocalDateTime.now());
        return this.updateById(article);
    }

    @Override
    @Transactional
    public boolean setArticleTop(Long id, Integer isTop) {
        Article article = new Article();
        article.setId(id);
        article.setIsTop(isTop);
        article.setUpdateTime(LocalDateTime.now());
        return this.updateById(article);
    }

    @Override
    @Transactional
    public boolean setArticleRecommend(Long id, Integer isRecommend) {
        Article article = new Article();
        article.setId(id);
        article.setIsRecommend(isRecommend);
        article.setUpdateTime(LocalDateTime.now());
        return this.updateById(article);
    }

    /**
     * 实体转DTO
     */
    private ArticleDTO convertToDTO(Article article) {
        ArticleDTO dto = new ArticleDTO();
        BeanUtils.copyProperties(article, dto);
        
        // 设置分类名称
        if (article.getCategoryId() != null) {
            ArticleCategory category = articleCategoryService.getById(article.getCategoryId());
            if (category != null) {
                dto.setCategoryName(category.getName());
            }
        }
        
        // 设置状态名称
        dto.setStatusName(getStatusName(article.getStatus()));
        
        // 处理标签
        if (StringUtils.hasText(article.getTags())) {
            dto.setTagArray(article.getTags().split(","));
        }
        
        return dto;
    }

    /**
     * DTO转实体
     */
    private Article convertToEntity(ArticleDTO dto) {
        Article article = new Article();
        BeanUtils.copyProperties(dto, article);
        
        // 处理标签数组
        if (dto.getTagArray() != null && dto.getTagArray().length > 0) {
            article.setTags(String.join(",", dto.getTagArray()));
        }
        
        return article;
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "草稿";
            case 1:
                return "已发布";
            case 2:
                return "已下架";
            default:
                return "未知";
        }
    }
} 