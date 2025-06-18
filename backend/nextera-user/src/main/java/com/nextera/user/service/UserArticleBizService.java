package com.nextera.user.service;

import com.nextera.api.article.dto.ArticleCreateRequest;
import com.nextera.api.article.dto.ArticleDTO;
import com.nextera.common.core.Result;
import com.nextera.user.client.ArticleServiceClient;
import com.nextera.user.dto.UserInfoDTO;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户文章业务服务类
 * 处理用户文章相关的业务逻辑
 * 
 * 支持两种分布式事务模式：
 * 1. AT模式（Automatic Transaction）：
 *    - 自动生成反向SQL，无需业务代码参与回滚
 *    - 适用于大部分CRUD场景
 *    - 实现简单，性能较高
 * 
 * 2. TCC模式（Try-Confirm-Cancel）：
 *    - 委托给 UserArticleBizTCCService 处理
 *    - 适用于需要精确控制事务阶段的复杂业务场景
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
    private final UserArticleBizTCCService userArticleBizTCCService;
    /**
     * 用户创建文章业务逻辑
     * 使用Seata分布式事务确保强一致性：要么都成功，要么都失败
     */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 30000)
    public Result<Integer> createArticle(ArticleCreateRequest request, Long userId, HttpServletRequest httpRequest) {
        log.info("用户创建文章业务处理，用户ID: {}, 文章标题: {}", userId, request.getTitle());
        
        try {
            // 1. 验证用户是否存在
            UserInfoDTO userInfoDTO = localUserService.getUserInfo(userId);
            if (userInfoDTO == null) {
                return Result.error("用户不存在");
            }
            
            // 2. 获取请求信息
            String ipAddress = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            // 3. 先更新用户最后活动时间（这是本地事务操作）
            boolean updateResult = localUserService.updateLastLoginTime(userId);
            if (!updateResult) {
                log.error("更新用户最后活动时间失败，用户ID: {}", userId);
                throw new RuntimeException("更新用户活动时间失败");
            }
            
            // 4. 通过OpenFeign调用文章服务创建文章（远程事务操作）
            Result<Integer> result = articleServiceClient.createArticle(
                request, userId, userInfoDTO.getUsername(), ipAddress, userAgent);
            
            if (result.isSuccess()) {
                log.info("用户 {} 成功创建文章并更新活动时间: {}", userInfoDTO.getUsername(), request.getTitle());
                return result;
            } else {
                log.error("用户 {} 创建文章失败: {}，将回滚整个事务", userInfoDTO.getUsername(), result.getMessage());
                // 抛出异常，触发Seata分布式事务回滚
                throw new RuntimeException("文章创建失败: " + result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("用户创建文章业务异常，将回滚整个分布式事务", e);
            // 重新抛出异常，确保Seata分布式事务回滚
            throw new RuntimeException("创建文章失败：" + e.getMessage(), e);
        }
    }


    /**
     * 用户更新文章业务逻辑 - TCC模式实现
     * 直接委托给TCC服务处理
     */
    public Result<Boolean> updateArticle(Long articleId, ArticleCreateRequest request, Long userId, HttpServletRequest httpRequest) {
        log.info("用户更新文章业务处理（TCC模式），用户ID: {}, 文章标题: {}", userId, request.getTitle());
        return userArticleBizTCCService.updateArticleTCC(articleId, request, userId, httpRequest);
    }

    /**
     * 用户更新文章业务逻辑 - 原有AT模式实现（保留）
     * 使用Seata分布式事务确保强一致性：要么都成功，要么都失败
     */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 30000)
    public Result<Boolean> updateArticleAT(Long articleId, ArticleCreateRequest request, Long userId, HttpServletRequest httpRequest) {
        log.info("用户更新文章业务处理（AT模式），用户ID: {}, 文章标题: {}", userId, request.getTitle());

        try {
            // 1. 验证用户是否存在
            UserInfoDTO userInfoDTO = localUserService.getUserInfo(userId);
            if (userInfoDTO == null) {
                return Result.error("用户不存在");
            }

            // 2. 获取请求信息
            String ipAddress = getClientIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");

            // 3. 先更新用户最后活动时间（这是本地事务操作）
            boolean updateResult = localUserService.updateLastLoginTime(userId);
            if (!updateResult) {
                log.error("更新用户最后活动时间失败，用户ID: {}", userId);
                throw new RuntimeException("更新用户活动时间失败");
            }

            // 4. 通过OpenFeign调用文章服务更新文章（远程事务操作）
            Result<Boolean> result = articleServiceClient.updateArticle(articleId, request, userId, userInfoDTO.getUsername(), ipAddress, userAgent);

            if (result.isSuccess()) {
                log.info("用户 {} 成功更新文章并更新活动时间: {}", userInfoDTO.getUsername(), request.getTitle());
                return result;
            } else {
                log.error("用户 {} 更新失败: {}，将回滚整个事务", userInfoDTO.getUsername(), result.getMessage());
                // 抛出异常，触发Seata分布式事务回滚
                throw new RuntimeException("文章更新失败: " + result.getMessage());
            }

        } catch (Exception e) {
            log.error("用户更新文章业务异常，将回滚整个分布式事务", e);
            // 重新抛出异常，确保Seata分布式事务回滚
            throw new RuntimeException("更新文章失败：" + e.getMessage(), e);
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