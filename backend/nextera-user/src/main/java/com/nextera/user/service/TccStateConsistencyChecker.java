package com.nextera.user.service;

import com.nextera.user.service.tcc.TccActionState;
import com.nextera.user.service.tcc.TccStateManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * TCC状态一致性检查器
 * 用于诊断和修复TCC状态不一致问题
 *
 * @author Scout
 * @date 2025-06-18
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TccStateConsistencyChecker {

    private final TccStateManager tccStateManager;

    /**
     * 检查TCC状态一致性
     * @param xid 全局事务ID
     * @return 检查报告
     */
    public String checkConsistency(String xid) {
        StringBuilder report = new StringBuilder();
        report.append("=== TCC状态一致性检查报告 ===\n");
        report.append("XID: ").append(xid).append("\n\n");

        try {
            // 检查用户TCC状态
            TccActionState userState = tccStateManager.getState(xid, "userLastLoginTimeTccAction");
            if (userState != null) {
                report.append("用户TCC状态: ").append(userState.getStatus()).append("\n");
                report.append("重试次数: ").append(userState.getRetryCount()).append("/").append(userState.getMaxRetryCount()).append("\n");
                report.append("创建时间: ").append(userState.getCreateTime()).append("\n");
                report.append("业务数据: ").append(userState.getBusinessData()).append("\n");
                report.append("原始数据: ").append(userState.getOriginalData()).append("\n\n");
            } else {
                report.append("用户TCC状态: 未找到\n\n");
            }

            // 检查文章TCC状态
            TccActionState articleState = tccStateManager.getState(xid, "articleUpdateTccAction");
            if (articleState != null) {
                report.append("文章TCC状态: ").append(articleState.getStatus()).append("\n");
                report.append("重试次数: ").append(articleState.getRetryCount()).append("/").append(articleState.getMaxRetryCount()).append("\n");
                report.append("创建时间: ").append(articleState.getCreateTime()).append("\n");
                report.append("业务数据: ").append(articleState.getBusinessData()).append("\n");
                report.append("原始数据: ").append(articleState.getOriginalData()).append("\n\n");
            } else {
                report.append("文章TCC状态: 未找到\n\n");
            }

            // 分析状态一致性
            if (userState != null && articleState != null) {
                report.append("=== 状态一致性分析 ===\n");
                
                if (TccActionState.TccStatus.COMMITTED.equals(userState.getStatus()) && 
                    TccActionState.TccStatus.COMMITTED.equals(articleState.getStatus())) {
                    report.append("✅ 状态一致：两个TCC都已提交\n");
                } else if (TccActionState.TccStatus.ROLLBACKED.equals(userState.getStatus()) && 
                           TccActionState.TccStatus.ROLLBACKED.equals(articleState.getStatus())) {
                    report.append("✅ 状态一致：两个TCC都已回滚\n");
                } else if (TccActionState.TccStatus.ROLLBACKED.equals(userState.getStatus()) && 
                           TccActionState.TccStatus.COMMITTED.equals(articleState.getStatus())) {
                    report.append("❌ 状态不一致：用户TCC已回滚，但文章TCC已提交\n");
                    report.append("建议：检查业务数据，可能需要手动修复\n");
                } else if (TccActionState.TccStatus.COMMITTED.equals(userState.getStatus()) && 
                           TccActionState.TccStatus.ROLLBACKED.equals(articleState.getStatus())) {
                    report.append("❌ 状态不一致：用户TCC已提交，但文章TCC已回滚\n");
                    report.append("建议：检查业务数据，可能需要手动修复\n");
                } else if (TccActionState.TccStatus.FAILED.equals(articleState.getStatus()) && 
                           TccActionState.TccStatus.ROLLBACKED.equals(userState.getStatus())) {
                    report.append("⚠️ 预期状态：文章TCC失败，用户TCC已回滚（符合预期）\n");
                } else {
                    report.append("⚠️ 其他状态组合：用户=").append(userState.getStatus())
                          .append(", 文章=").append(articleState.getStatus()).append("\n");
                }
            }

        } catch (Exception e) {
            report.append("检查过程中发生异常: ").append(e.getMessage()).append("\n");
            log.error("TCC状态一致性检查异常", e);
        }

        report.append("\n=== 检查完成 ===");
        return report.toString();
    }

    /**
     * 尝试修复状态不一致问题
     * @param xid 全局事务ID
     * @return 修复报告
     */
    public String attemptRepair(String xid) {
        StringBuilder report = new StringBuilder();
        report.append("=== TCC状态修复尝试 ===\n");
        report.append("XID: ").append(xid).append("\n\n");

        try {
            TccActionState userState = tccStateManager.getState(xid, "userLastLoginTimeTccAction");
            TccActionState articleState = tccStateManager.getState(xid, "articleUpdateTccAction");

            if (userState == null || articleState == null) {
                report.append("❌ 无法修复：缺少必要的TCC状态信息\n");
                return report.toString();
            }

            // 检查是否需要修复
            if (TccActionState.TccStatus.ROLLBACKED.equals(userState.getStatus()) && 
                TccActionState.TccStatus.FAILED.equals(articleState.getStatus())) {
                
                report.append("检测到：用户TCC=ROLLBACKED, 文章TCC=FAILED\n");
                report.append("这种情况下，如果Seata仍在重试Confirm，需要限制重试次数\n");
                
                // 增加重试计数到最大值，强制停止重试
                userState.setRetryCount(userState.getMaxRetryCount() + 1);
                tccStateManager.saveStateObject(userState);
                
                report.append("✅ 已设置用户TCC重试次数超过最大值，将在下次重试时停止\n");
                
            } else {
                report.append("当前状态组合不需要特殊修复\n");
            }

        } catch (Exception e) {
            report.append("修复过程中发生异常: ").append(e.getMessage()).append("\n");
            log.error("TCC状态修复异常", e);
        }

        report.append("\n=== 修复尝试完成 ===");
        return report.toString();
    }
} 