package com.nextera.article.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextera.article.dto.ArticleCreateRequest;
import com.nextera.article.dto.ArticleDTO;
import com.nextera.article.dubbo.ArticleService;
import com.nextera.article.entity.Article;
import com.nextera.article.entity.ArticleCategory;
import com.nextera.article.mapper.ArticleCategoryMapper;
import com.nextera.article.mapper.ArticleMapper;
import com.nextera.article.service.LocalArticleService;
import com.nextera.common.core.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private final ArticleService articleService;
    private final ArticleMapper articleMapper;
    private final ArticleCategoryMapper categoryMapper;

    @Override
    public Result<ArticleDTO> createArticle(ArticleCreateRequest request) {
        // 获取当前用户信息（这里简化处理，实际应该从认证上下文获取）
        Long userId = getCurrentUserId();
        String username = getCurrentUsername();
        String ipAddress = getClientIpAddress();
        String userAgent = getUserAgent();

        return articleService.createArticle(request, userId, username, ipAddress, userAgent);
    }

    @Override
    public Result<ArticleDTO> getArticleById(Long id) {
        return articleService.getArticleById(id);
    }

    @Override
    public Result<ArticleDTO> updateArticle(Long id, ArticleCreateRequest request) {
        Long userId = getCurrentUserId();
        String username = getCurrentUsername();
        String ipAddress = getClientIpAddress();
        String userAgent = getUserAgent();

        return articleService.updateArticle(id, request, userId, username, ipAddress, userAgent);
    }

    @Override
    public Result<Boolean> deleteArticle(Long id) {
        Long userId = getCurrentUserId();
        String username = getCurrentUsername();
        String ipAddress = getClientIpAddress();
        String userAgent = getUserAgent();

        return articleService.deleteArticle(id, userId, username, ipAddress, userAgent);
    }

    @Override
    public Result<Boolean> publishArticle(Long id) {
        Long userId = getCurrentUserId();
        String username = getCurrentUsername();
        String ipAddress = getClientIpAddress();
        String userAgent = getUserAgent();

        return articleService.publishArticle(id, userId, username, ipAddress, userAgent);
    }

    @Override
    public Result<Page<ArticleDTO>> getArticleList(Integer page, Integer size, Integer status, Long categoryId, Long authorId) {
        try {
            Page<Article> articlePage = new Page<>(page, size);
            
            LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
            if (status != null) {
                wrapper.eq(Article::getStatus, status);
            }
            if (categoryId != null) {
                wrapper.eq(Article::getCategoryId, categoryId);
            }
            if (authorId != null) {
                wrapper.eq(Article::getAuthorId, authorId);
            }
            
            wrapper.orderByDesc(Article::getCreateTime);
            
            Page<Article> result = articleMapper.selectPage(articlePage, wrapper);
            
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
                    },
                    (existing, replacement) -> existing
                ));
            
            // 转换为DTO
            Page<ArticleDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
            List<ArticleDTO> dtoList = result.getRecords().stream()
                .map(article -> {
                    ArticleDTO dto = new ArticleDTO();
                    BeanUtil.copyProperties(article, dto);
                    dto.setCategoryName(categoryMap.get(article.getCategoryId()));
                    return dto;
                })
                .collect(Collectors.toList());
            
            dtoPage.setRecords(dtoList);
            
            return Result.success(dtoPage);
            
        } catch (Exception e) {
            log.error("查询文章列表失败", e);
            return Result.error("查询文章列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前用户ID（简化实现，实际项目中应该从JWT或其他认证方式获取）
     */
    private Long getCurrentUserId() {
        // 这里简化处理，实际应该从JWT token或Spring Security上下文获取
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            String userIdStr = request.getHeader("User-Id");
            if (StrUtil.isNotBlank(userIdStr)) {
                return Long.parseLong(userIdStr);
            }
        }
        return 1L; // 默认用户ID
    }

    /**
     * 获取当前用户名
     */
    private String getCurrentUsername() {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            String username = request.getHeader("Username");
            if (StrUtil.isNotBlank(username)) {
                return username;
            }
        }
        return "admin"; // 默认用户名
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return "127.0.0.1";
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 处理多个IP的情况，取第一个
        if (StrUtil.isNotBlank(ip) && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return StrUtil.isBlank(ip) ? "127.0.0.1" : ip;
    }

    /**
     * 获取用户代理
     */
    private String getUserAgent() {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            return request.getHeader("User-Agent");
        }
        return "Unknown";
    }

    /**
     * 获取当前请求
     */
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
} 