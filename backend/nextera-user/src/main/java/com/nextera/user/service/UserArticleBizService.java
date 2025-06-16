package com.nextera.user.service;

import com.nextera.api.article.dto.ArticleCreateRequest;
import com.nextera.api.article.dto.ArticleDTO;
import com.nextera.common.core.Result;
import com.nextera.user.client.ArticleServiceClient;
import com.nextera.user.dto.UserInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户文章业务服务类
 * 处理用户文章相关的业务逻辑
 *
 * @author nextera
 * @since 2025-06-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserArticleBizService {

    private final ArticleServiceClient articleServiceClient;

    private final LocalUserService localUserService;
    /**
     * 用户创建文章业务逻辑
     */
    public Result<Integer> createArticle(ArticleCreateRequest request, Long userId, HttpServletRequest httpRequest) {
        log.info("用户创建文章业务处理，用户ID: {}, 文章标题: {}", userId, request.getTitle());
        
        try {
            // 1. 验证用户是否存在
            UserInfoDTO userInfoDTO = localUserService.getUserInfo(userId);
            if (userInfoDTO == null) {
                return Result.error("用户不存在");
            }
            

            // 2. 更新用户最后活动时间
            localUserService.updateLastLoginTime(userId);
            
            // 3. 获取请求信息
            String ipAddress = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            // 4. 通过Dubbo RPC调用文章服务创建文章
            Result<Integer> result = articleServiceClient.createArticle(
                request, userId, userInfoDTO.getUsername(), ipAddress, userAgent);
            
            if (result.isSuccess()) {
                log.info("用户 {} 成功创建文章: {}", userInfoDTO.getUsername(), request.getTitle());
            } else {
                log.error("用户 {} 创建文章失败: {}", userInfoDTO.getUsername(), result.getMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("用户创建文章业务异常", e);
            return Result.error("创建文章失败：" + e.getMessage());
        }
    }


    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
} 