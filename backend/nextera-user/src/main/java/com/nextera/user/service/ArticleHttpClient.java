package com.nextera.user.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nextera.api.article.dto.ArticleCreateRequest;
import com.nextera.api.article.dto.ArticleDTO;
import com.nextera.common.core.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * 文章服务HTTP客户端
 * 用于在TCC Confirm/Cancel阶段调用文章服务，避免Dubbo的Seata上下文传播问题
 * 
 * @author nextera
 * @since 2025-06-17
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleHttpClient {

    private final ObjectMapper objectMapper;
    
    @Value("${article.service.url:http://localhost:7082}")
    private String articleServiceUrl;
    
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    
    // 专门配置的ObjectMapper，确保正确处理泛型类型
    private ObjectMapper customObjectMapper;
    
    @PostConstruct
    public void initCustomObjectMapper() {
        this.customObjectMapper = new ObjectMapper();
        this.customObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.customObjectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        this.customObjectMapper.registerModule(new JavaTimeModule());
        this.customObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        log.info("ArticleHttpClient - 自定义ObjectMapper配置完成");
    }

    /**
     * 通过HTTP调用获取文章详情
     * 
     * @param articleId 文章ID
     * @return 文章详情
     */
    public Result<ArticleDTO> getArticleById(Long articleId) {
        try {
            String url = articleServiceUrl + "/article/" + articleId;
            log.info("HTTP调用获取文章详情，URL: {}", url);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                // 调试：打印原始响应内容
                String responseBody = response.body();
                log.info("HTTP调用获取文章详情 - 原始响应: {}", responseBody);
                
                // 使用自定义ObjectMapper和TypeReference正确解析泛型类型
                TypeReference<Result<ArticleDTO>> typeRef = new TypeReference<Result<ArticleDTO>>() {};
                Result<ArticleDTO> result = customObjectMapper.readValue(responseBody, typeRef);
                
                // 调试：检查解析后的数据类型
                if (result.getData() != null) {
                    log.info("HTTP调用获取文章详情 - 解析后数据类型: {}", result.getData().getClass().getName());
                    if (result.getData() instanceof ArticleDTO) {
                        ArticleDTO articleDTO = (ArticleDTO) result.getData();
                        log.info("HTTP调用获取文章详情 - 文章标题: {}, ID: {}", articleDTO.getTitle(), articleDTO.getId());
                    } else {
                        log.warn("HTTP调用获取文章详情 - 数据类型不匹配: {}", result.getData().getClass());
                    }
                }
                
                log.info("HTTP调用获取文章详情成功，文章ID: {}", articleId);
                return result;
            } else {
                log.error("HTTP调用获取文章详情失败，状态码: {}, 响应: {}", response.statusCode(), response.body());
                return Result.error("获取文章详情失败");
            }
            
        } catch (Exception e) {
            log.error("HTTP调用获取文章详情异常，文章ID: {}", articleId, e);
            return Result.error("获取文章详情异常: " + e.getMessage());
        }
    }

    /**
     * 通过HTTP调用更新文章
     * 
     * @param articleId 文章ID
     * @param request 更新请求
     * @return 更新结果
     */
    public Result<Boolean> updateArticle(Long articleId, ArticleDTO request) {
        try {
            String url = articleServiceUrl + "/article/" + articleId;
            log.info("HTTP调用更新文章，URL: {}, 标题: {}", url, request.getTitle());
            
            // 创建文章服务的DTO（直接使用API模块的DTO，与文章服务REST API兼容）
            ArticleCreateRequest articleRequest = new ArticleCreateRequest();
            articleRequest.setTitle(request.getTitle());
            articleRequest.setContent(request.getContent());
            articleRequest.setCategoryId(request.getCategoryId());
            articleRequest.setSummary(request.getSummary());
            articleRequest.setStatus(request.getStatus());
            articleRequest.setIsTop(request.getIsTop());
            articleRequest.setIsRecommend(request.getIsRecommend());
            articleRequest.setTags(request.getTags());
            articleRequest.setCoverImage(request.getCoverImage());

            String requestBody = customObjectMapper.writeValueAsString(articleRequest);
            
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                // 使用TypeReference正确解析泛型类型
                TypeReference<Result<Boolean>> typeRef = new TypeReference<Result<Boolean>>() {};
                Result<Boolean> result = customObjectMapper.readValue(response.body(), typeRef);
                log.info("HTTP调用更新文章成功，文章ID: {}, 标题: {}", articleId, request.getTitle());
                return result;
            } else {
                log.error("HTTP调用更新文章失败，状态码: {}, 响应: {}", response.statusCode(), response.body());
                return Result.error("更新文章失败");
            }
            
        } catch (Exception e) {
            log.error("HTTP调用更新文章异常，文章ID: {}, 标题: {}", articleId, request.getTitle(), e);
            return Result.error("更新文章异常: " + e.getMessage());
        }
    }
} 