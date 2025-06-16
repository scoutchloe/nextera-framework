package com.nextera.article.service;

import com.nextera.api.article.dto.ArticleCreateRequest;
import com.nextera.article.dto.ArticleDTO;
import com.nextera.common.core.Result;
import io.swagger.v3.oas.annotations.servers.Server;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author Scout
 * @date 2025-06-16 10:50
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class BizArticleService {

    private final LocalArticleService localArticleService;

    public Result<Integer> createArticleForDubbo(ArticleCreateRequest request, Long userId, String username,
                                                 String ipAddress, String userAgent) {
        ArticleDTO articleDTO = new ArticleDTO();
        articleDTO.setTitle(request.getTitle());
        articleDTO.setContent(request.getContent());
        articleDTO.setSummary(request.getSummary());
        articleDTO.setAuthorId(userId);
        articleDTO.setAuthorName(username);
        Integer ret = localArticleService.createArticleInner(articleDTO);
        return Result.success(ret);
    }
}