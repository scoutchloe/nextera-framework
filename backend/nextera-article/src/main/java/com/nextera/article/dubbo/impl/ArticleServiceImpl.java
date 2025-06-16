package com.nextera.article.dubbo.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextera.api.article.dto.ArticleCreateRequest;
import com.nextera.api.article.dto.ArticleDTO;
import com.nextera.api.article.service.ArticleService;
import com.nextera.article.entity.Article;
import com.nextera.article.entity.ArticleCategory;
import com.nextera.article.entity.UserArticle;
import com.nextera.article.mapper.ArticleCategoryMapper;
import com.nextera.article.mapper.ArticleMapper;
import com.nextera.article.mapper.UserArticleMapper;
import com.nextera.common.core.Result;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文章服务Dubbo实现类
 *
 * @author nextera
 * @since 2025-06-16
 */
@Slf4j
@Service
@DubboService(version = "1.0.0")
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleCategoryMapper categoryMapper;
    private final UserArticleMapper userArticleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<ArticleDTO> createArticle(ArticleCreateRequest request, Long userId, String username, String ipAddress, String userAgent) {
        try {
            // 转换为Article实体
            Article article = new Article();
            BeanUtil.copyProperties(request, article);
            
            // 设置作者信息
            article.setAuthorId(userId);
            article.setAuthorUsername(username);
            
            // 设置默认值
            article.setViewCount(0);
            article.setLikeCount(0);
            article.setCreateTime(LocalDateTime.now());
            article.setUpdateTime(LocalDateTime.now());
            
            // 如果状态为发布，设置发布时间
            if (article.getStatus() != null && article.getStatus() == 1) {
                article.setPublishTime(LocalDateTime.now());
            }
            
            // 保存文章
            articleMapper.insert(article);
            
            // 记录用户操作
            recordUserAction(userId, username, article.getId(), "CREATE", 
                "创建文章：" + article.getTitle(), ipAddress, userAgent);
            
            // 转换为DTO返回
            ArticleDTO articleDTO = convertToDTO(article);
            
            return Result.success(articleDTO);
            
        } catch (Exception e) {
            log.error("创建文章失败", e);
            return Result.error("创建文章失败：" + e.getMessage());
        }
    }

    @Override
    public Result<ArticleDTO> getArticleById(Long id) {
        try {
            Article article = articleMapper.selectById(id);
            if (article == null) {
                return Result.error("文章不存在");
            }
            
            ArticleDTO articleDTO = convertToDTO(article);
            return Result.success(articleDTO);
            
        } catch (Exception e) {
            log.error("获取文章详情失败", e);
            return Result.error("获取文章详情失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<ArticleDTO> updateArticle(Long id, ArticleCreateRequest request, Long userId, String username, String ipAddress, String userAgent) {
        try {
            // 检查文章是否存在
            Article existingArticle = articleMapper.selectById(id);
            if (existingArticle == null) {
                return Result.error("文章不存在");
            }
            
            // 转换为Article实体
            Article article = new Article();
            BeanUtil.copyProperties(request, article);
            article.setId(id);
            article.setUpdateTime(LocalDateTime.now());
            
            // 如果状态变为发布，设置发布时间
            if (article.getStatus() != null && article.getStatus() == 1 && existingArticle.getStatus() != 1) {
                article.setPublishTime(LocalDateTime.now());
            }
            
            // 更新文章
            articleMapper.updateById(article);
            
            // 记录用户操作
            recordUserAction(userId, username, id, "UPDATE", 
                "更新文章：" + article.getTitle(), ipAddress, userAgent);
            
            // 获取更新后的文章
            Article updatedArticle = articleMapper.selectById(id);
            ArticleDTO articleDTO = convertToDTO(updatedArticle);
            
            return Result.success(articleDTO);
            
        } catch (Exception e) {
            log.error("更新文章失败", e);
            return Result.error("更新文章失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> deleteArticle(Long id, Long userId, String username, String ipAddress, String userAgent) {
        try {
            // 检查文章是否存在
            Article article = articleMapper.selectById(id);
            if (article == null) {
                return Result.error("文章不存在");
            }
            
            // 删除文章
            int result = articleMapper.deleteById(id);
            
            if (result > 0) {
                // 记录用户操作
                recordUserAction(userId, username, id, "DELETE", 
                    "删除文章：" + article.getTitle(), ipAddress, userAgent);
                return Result.success(true);
            } else {
                return Result.error("删除失败");
            }
            
        } catch (Exception e) {
            log.error("删除文章失败", e);
            return Result.error("删除文章失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> publishArticle(Long id, Long userId, String username, String ipAddress, String userAgent) {
        try {
            // 检查文章是否存在
            Article article = articleMapper.selectById(id);
            if (article == null) {
                return Result.error("文章不存在");
            }
            
            // 更新文章状态为已发布
            article.setStatus(1);
            article.setPublishTime(LocalDateTime.now());
            article.setUpdateTime(LocalDateTime.now());
            
            int result = articleMapper.updateById(article);
            
            if (result > 0) {
                // 记录用户操作
                recordUserAction(userId, username, id, "PUBLISH", 
                    "发布文章：" + article.getTitle(), ipAddress, userAgent);
                return Result.success(true);
            } else {
                return Result.error("发布失败");
            }
            
        } catch (Exception e) {
            log.error("发布文章失败", e);
            return Result.error("发布文章失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Page<ArticleDTO>> getArticleList(Integer page, Integer size, Integer status, Long categoryId, Long authorId) {
        try {
            Page<Article> articlePage = new Page<>(page, size);
            
            LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(status != null, Article::getStatus, status)
                       .eq(categoryId != null, Article::getCategoryId, categoryId)
                       .eq(authorId != null, Article::getAuthorId, authorId)
                       .orderByDesc(Article::getCreateTime);
            
            Page<Article> result = articleMapper.selectPage(articlePage, queryWrapper);
            
            // 获取所有分类信息用于转换
            List<Long> categoryIds = result.getRecords().stream()
                .map(Article::getCategoryId)
                .distinct()
                .collect(Collectors.toList());
            
            Map<Long, String> categoryMap = categoryIds.stream()
                .collect(Collectors.toMap(
                    id -> id,
                    id -> {
                        ArticleCategory category = categoryMapper.selectById(id);
                        return category != null ? category.getName() : "未分类";
                    }
                ));
            
            // 转换为DTO
            List<ArticleDTO> dtoList = result.getRecords().stream()
                .map(article -> {
                    ArticleDTO dto = convertToDTO(article);
                    dto.setCategoryName(categoryMap.get(article.getCategoryId()));
                    return dto;
                })
                .collect(Collectors.toList());
            
            Page<ArticleDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
            dtoPage.setRecords(dtoList);
            
            return Result.success(dtoPage);
            
        } catch (Exception e) {
            log.error("获取文章列表失败", e);
            return Result.error("获取文章列表失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Long> getArticleCountByUserId(Long userId) {
        try {
            LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Article::getAuthorId, userId);
            
            Long count = articleMapper.selectCount(queryWrapper);
            return Result.success(count);
            
        } catch (Exception e) {
            log.error("获取用户文章数量失败", e);
            return Result.error("获取用户文章数量失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Page<ArticleDTO>> getArticlesByUserId(Long userId, Integer page, Integer size) {
        try {
            Page<Article> articlePage = new Page<>(page, size);
            
            LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Article::getAuthorId, userId)
                       .orderByDesc(Article::getCreateTime);
            
            Page<Article> result = articleMapper.selectPage(articlePage, queryWrapper);
            
            // 转换为DTO
            List<ArticleDTO> dtoList = result.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
            
            Page<ArticleDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
            dtoPage.setRecords(dtoList);
            
            return Result.success(dtoPage);
            
        } catch (Exception e) {
            log.error("获取用户文章列表失败", e);
            return Result.error("获取用户文章列表失败：" + e.getMessage());
        }
    }

    /**
     * 记录用户操作
     */
    private void recordUserAction(Long userId, String username, Long articleId, String actionType, String actionDesc, String ipAddress, String userAgent) {
        try {
            UserArticle userArticle = new UserArticle();
            userArticle.setUserId(userId);
            userArticle.setUsername(username);
            userArticle.setArticleId(articleId);
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
     * 转换为DTO
     */
    private ArticleDTO convertToDTO(Article article) {
        ArticleDTO dto = new ArticleDTO();
        BeanUtil.copyProperties(article, dto);
        
        // 获取分类名称
        if (article.getCategoryId() != null) {
            ArticleCategory category = categoryMapper.selectById(article.getCategoryId());
            if (category != null) {
                dto.setCategoryName(category.getName());
            }
        }
        
        return dto;
    }
} 