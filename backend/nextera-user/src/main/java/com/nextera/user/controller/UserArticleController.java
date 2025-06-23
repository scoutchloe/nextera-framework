package com.nextera.user.controller;

import com.nextera.api.article.dto.ArticleCreateRequest;
import com.nextera.common.core.Result;
import com.nextera.user.service.UserArticleBizService;
import com.nextera.user.service.UserArticleRocketMQService;
import com.nextera.user.service.DubboSeataFilterVerifier;
//import io.seata.spring.annotation.GlobalTransactional;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 用户文章控制器
 * 通过Dubbo RPC调用文章服务，实现用户写文章功能
 * 写文章的同时记录用户操作记录
 * 
 * 支持两种分布式事务模式：
 * - 默认使用TCC模式（委托给UserArticleBizTCCService）
 * - 保留AT模式接口（通过UserArticleBizService）
 *
 * @author nextera
 * @since 2025-06-16
 */
@Slf4j
@RestController
@RequestMapping("/api/user/article")
@RequiredArgsConstructor
@Validated
@Tag(name = "用户文章管理", description = "用户通过RPC调用文章服务的相关接口")
public class UserArticleController {

    private final UserArticleBizService userArticleBizService;
    private final UserArticleRocketMQService userArticleRocketMQService;
    private final DubboSeataFilterVerifier dubboSeataFilterVerifier;

    /**
     * 用户创建文章
     * 通过OpenFeign/Dubbo调用文章服务，使用Seata分布式事务确保数据一致性
     */
    @PostMapping("/create")
    @Operation(summary = "用户创建文章", description = "用户通过分布式事务创建文章，确保数据一致性")
    public Result<Integer> createArticle(
            @Valid @RequestBody ArticleCreateRequest request,
            @Parameter(description = "用户ID")
            @RequestParam(name="userId")  Long userId,
            HttpServletRequest httpRequest) {
        
        log.info("接收用户创建文章请求，用户ID: {}, 文章标题: {}", userId, request.getTitle());
        
        try {
            return userArticleBizService.createArticle(request, userId, httpRequest);
        } catch (Exception e) {
            log.error("用户创建文章失败，用户ID: {}, 标题: {}", userId, request.getTitle(), e);
            return Result.error("创建文章失败：" + e.getMessage());
        }
    }


    /**
     * 用户更新文章 - AT模式（保留接口）
     * 通过OpenFeign/Dubbo调用文章服务，使用Seata AT模式分布式事务确保数据一致性
     */
    @PostMapping("/update-at")
    @Operation(summary = "用户更新文章（AT模式）", description = "用户通过AT模式分布式事务更新文章，确保数据一致性")
    public Result<Boolean> updateArticleAT(
            @Valid @RequestBody ArticleCreateRequest request,
            @Parameter(description = "用户ID")
            @RequestParam(name="userId")  Long userId,
            @Parameter(description = "文章ID")
            @RequestParam(name="articleId")  Long articleId,
            HttpServletRequest httpRequest) {

        log.info("接收用户更新文章请求（AT模式），用户ID: {}, 文章标题: {}", userId, request.getTitle());

        try {
            return userArticleBizService.updateArticleAT(articleId, request, userId, httpRequest);
        } catch (Exception e) {
            log.error("用户更新文章失败（AT模式），用户ID: {}, 标题: {}", userId, request.getTitle(), e);
            return Result.error("更新文章失败：" + e.getMessage());
        }
    }

    /**
     * 用户更新文章 - RocketMQ事务消息模式
     * 使用RocketMQ事务消息实现分布式事务，确保数据最终一致性
     */
    @PostMapping("/update-rocketmq")
    @Operation(summary = "用户更新文章（RocketMQ事务消息）", description = "用户通过RocketMQ事务消息实现分布式事务更新文章，保证最终一致性")
    public Result<Boolean> updateArticleWithRocketMQ(
            @Valid @RequestBody ArticleCreateRequest request,
            @Parameter(description = "用户ID")
            @RequestParam(name="userId")  Long userId,
            @Parameter(description = "文章ID")
            @RequestParam(name="articleId")  Long articleId,
            HttpServletRequest httpRequest) {

        log.info("接收用户更新文章请求（RocketMQ事务消息），用户ID: {}, 文章标题: {}", userId, request.getTitle());

        try {
            return userArticleRocketMQService.updateArticleWithRocketMQ(articleId, request, userId, httpRequest);
        } catch (Exception e) {
            log.error("用户更新文章失败（RocketMQ事务消息），用户ID: {}, 标题: {}", userId, request.getTitle(), e);
            return Result.error("更新文章失败：" + e.getMessage());
        }
    }
} 