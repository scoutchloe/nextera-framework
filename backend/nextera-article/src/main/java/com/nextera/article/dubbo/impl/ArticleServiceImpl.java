package com.nextera.article.dubbo.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nextera.article.dubbo.ArticleService;
import com.nextera.article.dto.ArticleCreateRequest;
import com.nextera.article.dto.ArticleDTO;
import com.nextera.article.entity.Article;
import com.nextera.article.entity.ArticleCategory;
import com.nextera.article.entity.UserArticle;
import com.nextera.article.mapper.ArticleCategoryMapper;
import com.nextera.article.mapper.ArticleMapper;
import com.nextera.article.mapper.UserArticleMapper;
import com.nextera.common.core.Result;
import com.nextera.common.core.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 文章服务Dubbo实现类
 *
 * @author nextera
 * @since 2025-06-16
 */
@Slf4j
@Service
@DubboService
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleCategoryMapper categoryMapper;
    private final UserArticleMapper userArticleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<ArticleDTO> createArticle(ArticleCreateRequest request, Long userId, String username, String ipAddress, String userAgent) {
        try {
            // 验证分类是否存在
            ArticleCategory category = categoryMapper.selectById(request.getCategoryId());
            if (category == null) {
                return Result.error(ResultCode.PARAM_ERROR, "分类不存在");
            }

            // 创建文章
            Article article = new Article();
            BeanUtil.copyProperties(request, article);
            article.setAuthorId(userId);
            article.setAuthorName(username);
            article.setViewCount(0L);
            article.setLikeCount(0L);
            article.setCommentCount(0L);
            article.setCreateBy(userId);
            article.setUpdateBy(userId);

            // 如果没有提供摘要，自动生成
            if (StrUtil.isBlank(article.getSummary()) && StrUtil.isNotBlank(article.getContent())) {
                String summary = article.getContent().length() > 200 ? 
                    article.getContent().substring(0, 200) + "..." : article.getContent();
                article.setSummary(summary);
            }

            int result = articleMapper.insert(article);
            if (result <= 0) {
                return Result.error(ResultCode.OPERATION_ERROR, "创建文章失败");
            }

            // 记录用户操作
            recordUserAction(article.getId(), article.getTitle(), userId, username, 
                           1, "创建文章", ipAddress, userAgent);

            // 更新分类文章数量
            updateCategoryArticleCount(request.getCategoryId());

            // 构造返回结果
            ArticleDTO articleDTO = convertToDTO(article, category.getName());
            return Result.success(articleDTO);

        } catch (Exception e) {
            log.error("创建文章失败", e);
            return Result.error(ResultCode.OPERATION_ERROR, "创建文章失败: " + e.getMessage());
        }
    }

    @Override
    public Result<ArticleDTO> getArticleById(Long id) {
        try {
            Article article = articleMapper.selectById(id);
            if (article == null) {
                return Result.error(ResultCode.DATA_NOT_FOUND, "文章不存在");
            }

            // 获取分类信息
            ArticleCategory category = categoryMapper.selectById(article.getCategoryId());
            String categoryName = category != null ? category.getName() : "未分类";

            ArticleDTO articleDTO = convertToDTO(article, categoryName);
            return Result.success(articleDTO);

        } catch (Exception e) {
            log.error("获取文章失败", e);
            return Result.error(ResultCode.OPERATION_ERROR, "获取文章失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<ArticleDTO> updateArticle(Long id, ArticleCreateRequest request, Long userId, String username, String ipAddress, String userAgent) {
        try {
            Article existingArticle = articleMapper.selectById(id);
            if (existingArticle == null) {
                return Result.error(ResultCode.DATA_NOT_FOUND, "文章不存在");
            }

            // 检查权限（只有作者可以修改）
            if (!existingArticle.getAuthorId().equals(userId)) {
                return Result.error(ResultCode.FORBIDDEN, "无权限修改此文章");
            }

            // 验证分类是否存在
            ArticleCategory category = categoryMapper.selectById(request.getCategoryId());
            if (category == null) {
                return Result.error(ResultCode.PARAM_ERROR, "分类不存在");
            }

            // 更新文章
            Article article = new Article();
            BeanUtil.copyProperties(request, article);
            article.setId(id);
            article.setUpdateBy(userId);

            // 如果没有提供摘要，自动生成
            if (StrUtil.isBlank(article.getSummary()) && StrUtil.isNotBlank(article.getContent())) {
                String summary = article.getContent().length() > 200 ? 
                    article.getContent().substring(0, 200) + "..." : article.getContent();
                article.setSummary(summary);
            }

            int result = articleMapper.updateById(article);
            if (result <= 0) {
                return Result.error(ResultCode.OPERATION_ERROR, "更新文章失败");
            }

            // 记录用户操作
            recordUserAction(id, request.getTitle(), userId, username, 
                           2, "编辑文章", ipAddress, userAgent);

            // 如果分类发生变化，更新分类文章数量
            if (!existingArticle.getCategoryId().equals(request.getCategoryId())) {
                updateCategoryArticleCount(existingArticle.getCategoryId());
                updateCategoryArticleCount(request.getCategoryId());
            }

            // 获取更新后的文章
            Article updatedArticle = articleMapper.selectById(id);
            ArticleDTO articleDTO = convertToDTO(updatedArticle, category.getName());
            return Result.success(articleDTO);

        } catch (Exception e) {
            log.error("更新文章失败", e);
            return Result.error(ResultCode.OPERATION_ERROR, "更新文章失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> deleteArticle(Long id, Long userId, String username, String ipAddress, String userAgent) {
        try {
            Article article = articleMapper.selectById(id);
            if (article == null) {
                return Result.error(ResultCode.DATA_NOT_FOUND, "文章不存在");
            }

            // 检查权限（只有作者可以删除）
            if (!article.getAuthorId().equals(userId)) {
                return Result.error(ResultCode.FORBIDDEN, "无权限删除此文章");
            }

            int result = articleMapper.deleteById(id);
            if (result <= 0) {
                return Result.error(ResultCode.OPERATION_ERROR, "删除文章失败");
            }

            // 记录用户操作
            recordUserAction(id, article.getTitle(), userId, username, 
                           4, "删除文章", ipAddress, userAgent);

            // 更新分类文章数量
            updateCategoryArticleCount(article.getCategoryId());

            return Result.success(true);

        } catch (Exception e) {
            log.error("删除文章失败", e);
            return Result.error(ResultCode.OPERATION_ERROR, "删除文章失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> publishArticle(Long id, Long userId, String username, String ipAddress, String userAgent) {
        try {
            Article article = articleMapper.selectById(id);
            if (article == null) {
                return Result.error(ResultCode.DATA_NOT_FOUND, "文章不存在");
            }

            // 检查权限（只有作者可以发布）
            if (!article.getAuthorId().equals(userId)) {
                return Result.error(ResultCode.FORBIDDEN, "无权限发布此文章");
            }

            // 更新文章状态为已发布
            Article updateArticle = new Article();
            updateArticle.setId(id);
            updateArticle.setStatus(1);
            updateArticle.setUpdateBy(userId);

            int result = articleMapper.updateById(updateArticle);
            if (result <= 0) {
                return Result.error(ResultCode.OPERATION_ERROR, "发布文章失败");
            }

            // 记录用户操作
            recordUserAction(id, article.getTitle(), userId, username, 
                           3, "发布文章", ipAddress, userAgent);

            return Result.success(true);

        } catch (Exception e) {
            log.error("发布文章失败", e);
            return Result.error(ResultCode.OPERATION_ERROR, "发布文章失败: " + e.getMessage());
        }
    }

    /**
     * 记录用户操作
     */
    private void recordUserAction(Long articleId, String articleTitle, Long userId, String username,
                                 Integer actionType, String actionDesc, String ipAddress, String userAgent) {
        try {
            UserArticle userArticle = new UserArticle();
            userArticle.setUserId(userId);
            userArticle.setUsername(username);
            userArticle.setArticleId(articleId);
            userArticle.setArticleTitle(articleTitle);
            userArticle.setActionType(actionType);
            userArticle.setActionDesc(actionDesc);
            userArticle.setIpAddress(ipAddress);
            userArticle.setUserAgent(userAgent);
            userArticle.setActionTime(LocalDateTime.now());

            userArticleMapper.insert(userArticle);
        } catch (Exception e) {
            log.error("记录用户操作失败", e);
        }
    }

    /**
     * 更新分类文章数量
     */
    private void updateCategoryArticleCount(Long categoryId) {
        try {
            LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Article::getCategoryId, categoryId)
                   .eq(Article::getStatus, 1); // 只统计已发布的文章
            Long count = articleMapper.selectCount(wrapper);

            ArticleCategory category = new ArticleCategory();
            category.setId(categoryId);
            category.setArticleCount(count);
            categoryMapper.updateById(category);
        } catch (Exception e) {
            log.error("更新分类文章数量失败", e);
        }
    }

    /**
     * 转换为DTO
     */
    private ArticleDTO convertToDTO(Article article, String categoryName) {
        ArticleDTO dto = new ArticleDTO();
        BeanUtil.copyProperties(article, dto);
        dto.setCategoryName(categoryName);
        return dto;
    }
} 