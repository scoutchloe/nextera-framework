package com.nextera.article.dubbo;

import com.nextera.api.article.dto.ArticleCreateRequest;
import com.nextera.api.article.dto.ArticleDTO;
import com.nextera.api.article.service.ArticleService;
import com.nextera.article.service.BizArticleService;
import com.nextera.common.core.Result;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

/**
 * @author Scout
 * @date 2025-06-16 10:31
 * @since 1.0
 */
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
    public Result<ArticleDTO> getArticleById(Long id) {
        return null;
    }
}