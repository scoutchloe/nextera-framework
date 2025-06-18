package com.nextera.article.service;

import com.nextera.api.article.dto.ArticleCreateRequest;
import com.nextera.article.dto.ArticleDTO;
import com.nextera.common.core.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 文章业务服务类
 * 处理文章相关的业务逻辑，支持分布式事务
 * 
 * @author Scout
 * @date 2025-06-16 10:50
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BizArticleService {

    private final LocalArticleService localArticleService;

    /**
     * 为Dubbo/OpenFeign调用创建文章
     * 此方法会被包含在分布式事务中
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> createArticleForDubbo(ArticleCreateRequest request, Long userId, String username,
                                                 String ipAddress, String userAgent) {
        log.info("开始创建文章，用户ID: {}, 用户名: {}, 标题: {}", userId, username, request.getTitle());
        
        try {
            // 构建文章DTO
            ArticleDTO articleDTO = new ArticleDTO();
            articleDTO.setTitle(request.getTitle());
            articleDTO.setContent(request.getContent());
            articleDTO.setSummary(request.getSummary());
            articleDTO.setAuthorId(userId);
            articleDTO.setAuthorName(username);
            articleDTO.setCategoryId(request.getCategoryId());
            articleDTO.setTags(request.getTags());
            articleDTO.setCoverImage(request.getCoverImage());
            articleDTO.setStatus(request.getStatus());
            articleDTO.setIsTop(request.getIsTop());
            articleDTO.setIsRecommend(request.getIsRecommend());
            
            // 调用本地服务创建文章
            Integer result = localArticleService.createArticleInner(articleDTO);
            
            if (result > 0) {
                log.info("文章创建成功，用户ID: {}, 用户名: {}, 标题: {}", userId, username, request.getTitle());
                return Result.success(result);
            } else {
                log.error("文章创建失败，返回值: {}, 用户ID: {}, 标题: {}", result, userId, request.getTitle());
                throw new RuntimeException("文章创建失败，数据库插入失败");
            }
            
        } catch (Exception e) {
            log.error("创建文章过程中发生异常，用户ID: {}, 标题: {}", userId, request.getTitle(), e);
            throw new RuntimeException("创建文章失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新文章
     * @param articleId
     * @param request
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> updateArticle(Long articleId, ArticleCreateRequest request) {
        try {
            log.info("开始更新文章，ID: {}, 标题: {}", articleId, request.getTitle());
            boolean ret = localArticleService.updateArticleInner(articleId, request);
            if (ret) {
                return Result.success(true);
            } else {
                log.info("更新文章失败： <UNK>ID: {}, <UNK>: {}", articleId, request.getTitle());
                throw new RuntimeException("更新文章失败.");
            }
        } catch (Exception e) {
            log.error("更新文章过程中发生异常，ID: {}, 标题: {}", articleId, request.getTitle(), e);
            throw new RuntimeException("更新文章失败: " + e.getMessage(), e);
        }
    }

    public Result<com.nextera.api.article.dto.ArticleDTO> getArticleById(Long id) {
        ArticleDTO articleDTO = localArticleService.getArticleById(id);
        com.nextera.api.article.dto.ArticleDTO ret = new com.nextera.api.article.dto.ArticleDTO();
        BeanUtils.copyProperties(articleDTO, ret);
        return Result.success(ret);

    }
}