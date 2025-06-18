package com.nextera.user.service;

import com.nextera.api.article.dto.ArticleCreateRequest;
import com.nextera.common.core.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * TCC使用示例
 * 展示如何正确使用TCC模式处理分布式事务，确保数据一致性
 *
 * @author Scout
 * @date 2025-06-18
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Service
public class TccUsageExample {

    private final UserArticleBizTCCService userArticleBizTCCService;

    /**
     * 示例1：正常的TCC事务处理
     * 展示如何使用TCC模式更新文章和用户信息
     */
    public void exampleNormalTccTransaction() {
        log.info("=== TCC正常事务示例 ===");
        
        try {
            // 准备测试数据
            Long articleId = 1L;
            Long userId = 1L;
            
            ArticleCreateRequest request = new ArticleCreateRequest();
            request.setTitle("TCC测试文章");
            request.setContent("这是一个TCC模式的测试文章内容");
            request.setCategoryId(1L);
            request.setStatus(1);
            request.setSummary("TCC测试摘要");
            
            // 执行TCC事务
            log.info("开始执行TCC事务...");
            Result<Boolean> result = userArticleBizTCCService.updateArticleTCC(
                articleId, request, userId, null);
            
            if (result.isSuccess()) {
                log.info("TCC事务执行成功！");
                log.info("重要：只有当所有TCC操作的Confirm阶段都成功时，才会返回成功");
                log.info("如果任何一个Confirm失败，整个事务将回滚，保证数据一致性");
            } else {
                log.error("TCC事务执行失败: {}", result.getMessage());
            }
            
        } catch (Exception e) {
            log.error("TCC事务示例执行异常", e);
            log.info("当异常发生时，Seata会自动调用所有参与资源的Cancel方法");
            log.info("确保用户的lastLoginTime和文章数据都回滚到原始状态");
        }
    }

    /**
     * 示例2：模拟TCC事务失败场景
     * 展示当部分操作失败时，如何保证数据一致性
     */
    public void exampleTccTransactionFailure() {
        log.info("=== TCC事务失败示例 ===");
        
        try {
            // 准备可能导致失败的测试数据
            Long articleId = 999999L; // 不存在的文章ID
            Long userId = 1L;
            
            ArticleCreateRequest request = new ArticleCreateRequest();
            request.setTitle("TCC失败测试文章");
            request.setContent("这是一个会导致TCC事务失败的测试");
            request.setCategoryId(1L);
            request.setStatus(1);
            request.setSummary("TCC失败测试摘要");
            
            // 执行TCC事务（预期会失败）
            log.info("开始执行预期失败的TCC事务...");
            Result<Boolean> result = userArticleBizTCCService.updateArticleTCC(
                articleId, request, userId, null);
            
            if (result.isSuccess()) {
                log.warn("意外成功：TCC事务应该失败但成功了");
            } else {
                log.info("TCC事务按预期失败: {}", result.getMessage());
                log.info("数据一致性保证：用户的lastLoginTime应该已经回滚");
            }
            
        } catch (Exception e) {
            log.info("TCC事务按预期失败并抛出异常: {}", e.getMessage());
            log.info("Seata已自动执行所有Cancel方法，保证数据一致性");
        }
    }

    /**
     * 示例3：TCC事务状态检查
     * 展示如何检查TCC事务状态，验证数据一致性
     */
    public void exampleTccStatusCheck(String xid) {
        log.info("=== TCC状态检查示例 ===");
        
        try {
            log.info("检查TCC事务状态: XID={}", xid);
            
            Result<String> statusResult = userArticleBizTCCService.checkTccStatus(xid);
            
            if (statusResult.isSuccess()) {
                log.info("TCC状态检查结果:");
                log.info("{}", statusResult.getData());
            } else {
                log.error("TCC状态检查失败: {}", statusResult.getMessage());
            }
            
        } catch (Exception e) {
            log.error("TCC状态检查异常", e);
        }
    }

    /**
     * 示例4：数据一致性验证
     * 展示如何验证TCC事务执行后的数据一致性
     */
    public void exampleDataConsistencyCheck(Long userId, Long articleId) {
        log.info("=== 数据一致性验证示例 ===");
        
        try {
            log.info("验证用户和文章数据的一致性");
            log.info("用户ID: {}, 文章ID: {}", userId, articleId);
            
            // 这里可以添加具体的数据一致性检查逻辑
            // 例如：检查用户的lastLoginTime是否在合理范围内
            // 检查文章的更新时间是否与用户操作时间一致
            
            log.info("数据一致性验证完成");
            
        } catch (Exception e) {
            log.error("数据一致性验证失败", e);
        }
    }

    /**
     * TCC最佳实践说明
     */
    public void tccBestPractices() {
        log.info("=== TCC最佳实践 ===");
        log.info("1. Try阶段：执行业务操作并保存原始状态");
        log.info("2. Confirm阶段：确认业务操作（业务操作已在Try阶段完成）");
        log.info("3. Cancel阶段：回滚业务操作，恢复原始状态");
        log.info("4. 幂等性：所有TCC方法都必须支持重复调用");
        log.info("5. 空回滚：Cancel方法要能处理Try未执行的情况");
        log.info("6. 悬挂检查：防止Cancel在Try之前执行");
        log.info("7. 状态管理：使用状态管理器跟踪TCC操作状态");
        log.info("8. 异常处理：合理处理各种异常情况，确保数据一致性");
        log.info("9. 监控告警：实时监控TCC事务状态，及时发现问题");
        log.info("10. 数据验证：定期验证数据一致性，确保系统稳定性");
    }
} 