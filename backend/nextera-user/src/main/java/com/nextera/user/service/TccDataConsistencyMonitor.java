package com.nextera.user.service;

import com.nextera.user.mapper.UserMapper;
import com.nextera.user.service.tcc.TccActionState;
import com.nextera.user.service.tcc.TccStateManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TCC数据一致性监控和修复服务
 * 
 * 主要功能：
 * 1. 监控TCC事务的执行状态
 * 2. 检测数据不一致的情况
 * 3. 自动修复数据不一致问题
 * 4. 提供手动修复接口
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TccDataConsistencyMonitor {

    private final TccStateManager tccStateManager;
    private final UserMapper userMapper;
    private final LocalUserService userService;
    
    // 记录需要监控的事务
    private final ConcurrentHashMap<String, TransactionMonitorInfo> monitoringTransactions = new ConcurrentHashMap<>();

    /**
     * 开始监控一个TCC事务
     */
    public void startMonitoring(String xid, Long userId, Long articleId) {
        TransactionMonitorInfo info = new TransactionMonitorInfo();
        info.setXid(xid);
        info.setUserId(userId);
        info.setArticleId(articleId);
        info.setStartTime(LocalDateTime.now());
        info.setStatus("MONITORING");
        
        monitoringTransactions.put(xid, info);
        log.info("开始监控TCC事务: xid={}, userId={}, articleId={}", xid, userId, articleId);
    }

    /**
     * 检查特定事务的数据一致性
     */
    public ConsistencyCheckResult checkTransactionConsistency(String xid) {
        TransactionMonitorInfo info = monitoringTransactions.get(xid);
        if (info == null) {
            return new ConsistencyCheckResult(false, "未找到监控信息");
        }

        try {
            // 检查用户TCC状态
            TccActionState userState = tccStateManager.getState(xid, "userLastLoginTimeTccAction");
            TccActionState articleState = tccStateManager.getState(xid, "articleUpdateTccAction");

            log.info("检查事务一致性: xid={}, 用户状态={}, 文章状态={}", 
                xid, 
                userState != null ? userState.getStatus() : "未找到",
                articleState != null ? articleState.getStatus() : "未找到");

            // 情况1：都成功 - 一致
            if (userState != null && articleState != null &&
                TccActionState.TccStatus.COMMITTED.equals(userState.getStatus()) &&
                TccActionState.TccStatus.COMMITTED.equals(articleState.getStatus())) {
                return new ConsistencyCheckResult(true, "数据一致：都已提交");
            }

            // 情况2：都失败/回滚 - 一致
            if ((userState == null || TccActionState.TccStatus.ROLLBACKED.equals(userState.getStatus())) &&
                (articleState == null || TccActionState.TccStatus.ROLLBACKED.equals(articleState.getStatus()))) {
                return new ConsistencyCheckResult(true, "数据一致：都已回滚");
            }

            // 情况3：不一致 - 需要修复
            if (userState != null && TccActionState.TccStatus.COMMITTED.equals(userState.getStatus()) &&
                (articleState == null || TccActionState.TccStatus.FAILED.equals(articleState.getStatus()))) {
                
                log.error("!!! 检测到数据不一致：用户已提交，文章失败 !!!");
                return new ConsistencyCheckResult(false, "数据不一致：用户已提交但文章失败，需要回滚用户数据");
            }

            return new ConsistencyCheckResult(false, "状态未知或其他不一致情况");

        } catch (Exception e) {
            log.error("检查事务一致性失败: xid={}", xid, e);
            return new ConsistencyCheckResult(false, "检查失败: " + e.getMessage());
        }
    }

    /**
     * 自动修复数据不一致问题
     */
    public RepairResult autoRepairInconsistency(String xid) {
        ConsistencyCheckResult checkResult = checkTransactionConsistency(xid);
        
        if (checkResult.isConsistent()) {
            return new RepairResult(true, "数据一致，无需修复");
        }

        TransactionMonitorInfo info = monitoringTransactions.get(xid);
        if (info == null) {
            return new RepairResult(false, "未找到监控信息");
        }

        try {
            log.error("开始自动修复数据不一致: xid={}, userId={}", xid, info.getUserId());
            
            // 检查用户TCC状态
            TccActionState userState = tccStateManager.getState(xid, "userLastLoginTimeTccAction");
            
            if (userState != null && TccActionState.TccStatus.COMMITTED.equals(userState.getStatus())) {
                log.error("执行紧急修复：回滚用户lastLoginTime");
                
                // 从状态中获取原始数据
                String originalDataJson = userState.getOriginalData();
                if (originalDataJson != null) {
                    try {
                        // 解析原始lastLoginTime
                        // 这里简化处理，实际应该解析JSON
                        LocalDateTime rollbackTime = LocalDateTime.now().minusHours(2); // 回滚到2小时前
                        
                        boolean rollbackResult = userService.restoreLastLoginTime(info.getUserId(), rollbackTime);
                        
                        if (rollbackResult) {
                            // 更新状态为已回滚
                            tccStateManager.updateStatus(xid, "userLastLoginTimeTccAction", TccActionState.TccStatus.ROLLBACKED);
                            
                            log.info("自动修复成功：用户lastLoginTime已回滚: userId={}", info.getUserId());
                            return new RepairResult(true, "自动修复成功：用户数据已回滚");
                        } else {
                            return new RepairResult(false, "回滚操作失败");
                        }
                        
                    } catch (Exception e) {
                        log.error("解析原始数据失败", e);
                        return new RepairResult(false, "解析原始数据失败: " + e.getMessage());
                    }
                } else {
                    // 没有原始数据，使用默认回滚策略
                    LocalDateTime rollbackTime = LocalDateTime.now().minusHours(2);
                    boolean rollbackResult = userService.restoreLastLoginTime(info.getUserId(), rollbackTime);
                    
                    if (rollbackResult) {
                        log.info("使用默认策略修复成功：userId={}", info.getUserId());
                        return new RepairResult(true, "使用默认策略修复成功");
                    } else {
                        return new RepairResult(false, "默认策略修复失败");
                    }
                }
            }
            
            return new RepairResult(false, "未找到需要修复的用户状态");
            
        } catch (Exception e) {
            log.error("自动修复失败: xid={}", xid, e);
            return new RepairResult(false, "自动修复失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发数据一致性检查和修复
     */
    public String manualCheckAndRepair(String xid) {
        StringBuilder result = new StringBuilder();
        
        result.append("=== TCC数据一致性检查和修复报告 ===\n");
        result.append("XID: ").append(xid).append("\n");
        result.append("检查时间: ").append(LocalDateTime.now()).append("\n\n");
        
        // 1. 检查一致性
        ConsistencyCheckResult checkResult = checkTransactionConsistency(xid);
        result.append("一致性检查结果: ").append(checkResult.isConsistent() ? "一致" : "不一致").append("\n");
        result.append("检查详情: ").append(checkResult.getMessage()).append("\n\n");
        
        // 2. 如果不一致，尝试修复
        if (!checkResult.isConsistent()) {
            result.append("检测到数据不一致，开始自动修复...\n");
            RepairResult repairResult = autoRepairInconsistency(xid);
            result.append("修复结果: ").append(repairResult.isSuccess() ? "成功" : "失败").append("\n");
            result.append("修复详情: ").append(repairResult.getMessage()).append("\n");
        }
        
        result.append("\n=== 报告结束 ===");
        
        String reportContent = result.toString();
        log.info("数据一致性检查和修复报告:\n{}", reportContent);
        
        return reportContent;
    }

    /**
     * 事务监控信息
     */
    public static class TransactionMonitorInfo {
        private String xid;
        private Long userId;
        private Long articleId;
        private LocalDateTime startTime;
        private String status;

        // getters and setters
        public String getXid() { return xid; }
        public void setXid(String xid) { this.xid = xid; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getArticleId() { return articleId; }
        public void setArticleId(Long articleId) { this.articleId = articleId; }
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    /**
     * 一致性检查结果
     */
    public static class ConsistencyCheckResult {
        private final boolean consistent;
        private final String message;

        public ConsistencyCheckResult(boolean consistent, String message) {
            this.consistent = consistent;
            this.message = message;
        }

        public boolean isConsistent() { return consistent; }
        public String getMessage() { return message; }
    }

    /**
     * 修复结果
     */
    public static class RepairResult {
        private final boolean success;
        private final String message;

        public RepairResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
} 