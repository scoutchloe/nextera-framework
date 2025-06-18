package com.nextera.article.dubbo;

import com.nextera.api.article.dto.ArticleCreateRequest;
import com.nextera.api.article.dto.ArticleDTO;
import com.nextera.api.article.service.ArticleService;
import com.nextera.article.service.BizArticleService;
import com.nextera.common.core.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

/**
 * @author Scout
 * @date 2025-06-16 10:31
 * @since 1.0
 */
@Slf4j
@Service
@DubboService(version = "1.0.0")
@RequiredArgsConstructor
public class DubboArticleService implements ArticleService {

    private final BizArticleService bizArticleService;


    @Override
    public Result<Integer> createArticle(ArticleCreateRequest request, Long userId, String username, String ipAddress, String userAgent) {
        return bizArticleService.createArticleForDubbo(request, userId, username, ipAddress, userAgent);
    }

    @Override
    public Result<Boolean> updateArticle(Long articleId, ArticleCreateRequest request, Long userId, String username, String ipAddress, String userAgent) {
        // 暂时忽略额外的参数，直接调用业务服务
        // 在实际应用中，这些参数可以用于日志记录、权限验证等
        log.info("更新文章请求: articleId={}, userId={}, username={}, ipAddress={}", 
            articleId, userId, username, ipAddress);
        return bizArticleService.updateArticle(articleId, request);
    }


    @Override
    public Result<ArticleDTO> getArticleById(Long id) {
        return bizArticleService.getArticleById(id);
    }
}