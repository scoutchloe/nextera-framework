package com.nextera.user.controller;

import com.nextera.api.article.dto.ArticleCreateRequest;
import com.nextera.common.core.Result;
import com.nextera.user.service.TccDataConsistencyMonitor;
import com.nextera.user.service.TccStateConsistencyChecker;
import com.nextera.user.service.UserArticleBizService;
import com.nextera.user.service.UserArticleBizTCCService;
import io.seata.spring.annotation.GlobalTransactional;
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
    private final UserArticleBizTCCService userArticleBizTCCService;
    private final TccDataConsistencyMonitor tccDataConsistencyMonitor;
    private final TccStateConsistencyChecker tccStateConsistencyChecker;

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
     * 用户更新文章 - 默认（TCC模式）
     * 通过TCC服务调用文章服务，使用Seata TCC模式分布式事务确保数据一致性
     */
    @PostMapping("/update")
    @Operation(summary = "用户更新文章（TCC模式）", description = "用户通过TCC模式分布式事务更新文章，提供精确的事务控制")
    public Result<Boolean> updateArticle(
            @Valid @RequestBody ArticleCreateRequest request,
            @Parameter(description = "用户ID")
            @RequestParam(name="userId")  Long userId,
            @Parameter(description = "文章ID")
            @RequestParam(name="articleId")  Long articleId,
            HttpServletRequest httpRequest) {

        log.info("接收用户更新文章请求（TCC模式），用户ID: {}, 文章标题: {}", userId, request.getTitle());

        try {
            return userArticleBizService.updateArticle(articleId, request, userId, httpRequest);
        } catch (Exception e) {
            log.error("用户更新文章失败（TCC模式），用户ID: {}, 标题: {}", userId, request.getTitle(), e);
            return Result.error("更新文章失败：" + e.getMessage());
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
     * 用户更新文章 - TCC模式
     * 通过OpenFeign/Dubbo调用文章服务，使用Seata TCC模式分布式事务确保数据一致性
     */
    @PostMapping("/update-tcc")
    @Operation(summary = "用户更新文章（TCC模式）2", description = "用户通过TCC模式分布式事务更新文章，提供更精确的事务控制")
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 30000)
    public Result<Boolean> updateArticleTCC(
            @Valid @RequestBody ArticleCreateRequest request,
            @Parameter(description = "用户ID")
            @RequestParam(name="userId")  Long userId,
            @Parameter(description = "文章ID")
            @RequestParam(name="articleId")  Long articleId,
            HttpServletRequest httpRequest) {

        log.info("接收用户更新文章请求（TCC模式），用户ID: {}, 文章标题: {}", userId, request.getTitle());

        try {
            // 1. 验证用户是否存在
            return userArticleBizTCCService.updateArticleTCC(articleId, request, userId, httpRequest);
        } catch (Exception e) {
            log.error("用户更新文章失败（TCC模式），用户ID: {}, 标题: {}", userId, request.getTitle(), e);
            return Result.error("更新文章失败：" + e.getMessage());
        }
    }

    /**
     * 检查TCC事务状态
     * 用于监控和验证TCC事务的数据一致性
     */
    @GetMapping("/tcc/status/{xid}")
    @Operation(summary = "检查TCC事务状态", description = "检查指定XID的TCC事务状态，用于数据一致性验证")
    public Result<String> checkTccStatus(
            @Parameter(description = "全局事务ID") 
            @PathVariable String xid) {

        log.info("接收TCC状态检查请求，XID: {}", xid);

        try {
            return userArticleBizTCCService.checkTccStatus(xid);
        } catch (Exception e) {
            log.error("TCC状态检查失败，XID: {}", xid, e);
            return Result.error("TCC状态检查失败：" + e.getMessage());
        }
    }

    /**
     * 验证TCC事务完整性
     * 用于检查和修复TCC数据一致性问题
     */
    @GetMapping("/tcc/verify")
    @Operation(summary = "验证TCC事务完整性", description = "验证指定事务的TCC数据一致性，并在必要时尝试修复")
    public Result<Boolean> verifyTccIntegrity(
            @Parameter(description = "全局事务ID") 
            @RequestParam String xid,
            @Parameter(description = "用户ID") 
            @RequestParam Long userId,
            @Parameter(description = "文章ID") 
            @RequestParam Long articleId) {

        log.info("接收TCC完整性验证请求，XID: {}, userId: {}, articleId: {}", xid, userId, articleId);

        try {
            return userArticleBizTCCService.verifyTccTransactionIntegrity(xid, userId, articleId);
        } catch (Exception e) {
            log.error("TCC完整性验证失败，XID: {}, userId: {}, articleId: {}", xid, userId, articleId, e);
            return Result.error("TCC完整性验证失败：" + e.getMessage());
        }
    }

    /**
     * 手动修复TCC数据一致性问题
     * 用于检测和修复TCC事务的数据不一致问题
     */
    @PostMapping("/tcc/repair/{xid}")
    @Operation(summary = "手动修复TCC数据一致性", description = "检查并修复指定XID的TCC事务数据一致性问题")
    public Result<String> repairTccConsistency2(
            @Parameter(description = "全局事务ID") 
            @PathVariable String xid) {

        log.info("接收TCC数据一致性修复请求，XID: {}", xid);

        try {
            String repairReport = tccDataConsistencyMonitor.manualCheckAndRepair(xid);
            return Result.success(repairReport);
        } catch (Exception e) {
            log.error("TCC数据一致性修复失败，XID: {}", xid, e);
            return Result.error("TCC数据一致性修复失败：" + e.getMessage());
        }
    }

    /**
     * 检查TCC状态一致性
     * 用于诊断TCC状态不一致问题
     */
    @GetMapping("/tcc/consistency/check/{xid}")
    @Operation(summary = "检查TCC状态一致性", description = "检查指定XID的TCC状态一致性，诊断状态不一致问题")
    public Result<String> checkTccConsistency(
            @Parameter(description = "全局事务ID") 
            @PathVariable String xid) {

        log.info("接收TCC状态一致性检查请求，XID: {}", xid);

        try {
            String checkReport = tccStateConsistencyChecker.checkConsistency(xid);
            return Result.success(checkReport);
        } catch (Exception e) {
            log.error("TCC状态一致性检查失败，XID: {}", xid, e);
            return Result.error("TCC状态一致性检查失败：" + e.getMessage());
        }
    }

    /**
     * 尝试修复TCC状态不一致问题
     * 用于自动修复特定的TCC状态不一致情况
     */
    @PostMapping("/tcc/consistency/repair/{xid}")
    @Operation(summary = "修复TCC状态不一致", description = "尝试自动修复指定XID的TCC状态不一致问题")
    public Result<String> repairTccConsistency(
            @Parameter(description = "全局事务ID") 
            @PathVariable String xid) {

        log.info("接收TCC状态一致性修复请求，XID: {}", xid);

        try {
            String repairReport = tccStateConsistencyChecker.attemptRepair(xid);
            return Result.success(repairReport);
        } catch (Exception e) {
            log.error("TCC状态一致性修复失败，XID: {}", xid, e);
            return Result.error("TCC状态一致性修复失败：" + e.getMessage());
        }
    }

} 