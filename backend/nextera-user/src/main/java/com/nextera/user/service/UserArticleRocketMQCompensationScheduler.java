package com.nextera.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nextera.user.entity.RocketmqTransactionLog;
import com.nextera.user.mapper.RocketmqTransactionLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RocketMQ事务补偿定时任务
 * 定期检查失败的事务并执行补偿操作
 *
 * @author nextera
 * @since 2025-06-23
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserArticleRocketMQCompensationScheduler {

    private final RocketmqTransactionLogMapper transactionLogMapper;
    private final UserArticleRocketMQCompensationService compensationService;

    /**
     * 定时检查需要补偿的事务
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 5 * 60 * 1000) // 5分钟
    public void checkAndCompensateFailedTransactions() {
        log.info("开始检查需要补偿的事务");
        
        try {
            // 查询本地事务已提交但消息状态为回滚的记录（需要补偿）
            List<RocketmqTransactionLog> needCompensationLogs = transactionLogMapper.selectList(
                    new LambdaQueryWrapper<RocketmqTransactionLog>()
                            .eq(RocketmqTransactionLog::getLocalTransactionStatus, 
                                RocketmqTransactionLog.LocalTransactionStatus.COMMITTED.getCode())
                            .eq(RocketmqTransactionLog::getMessageStatus, 
                                RocketmqTransactionLog.MessageStatus.ROLLBACK.getCode())
                            .notLike(RocketmqTransactionLog::getErrorMessage, "补偿成功") // 还未成功补偿过
                            .lt(RocketmqTransactionLog::getUpdatedTime, 
                                LocalDateTime.now().minusMinutes(2)) // 2分钟前的记录，避免处理过于新的记录
            );
            
            if (needCompensationLogs.isEmpty()) {
                log.debug("未发现需要补偿的事务");
                return;
            }
            
            log.info("发现{}个需要补偿的事务", needCompensationLogs.size());
            
            for (RocketmqTransactionLog transactionLog : needCompensationLogs) {
                executeCompensationForTransaction(transactionLog);
            }
            
        } catch (Exception e) {
            log.error("检查补偿事务时发生异常", e);
        }
    }

    /**
     * 为单个事务执行补偿
     */
    private void executeCompensationForTransaction(RocketmqTransactionLog transactionLog) {
        String transactionId = transactionLog.getTransactionId();
        
        try {
            log.info("开始为事务执行补偿: transactionId={}", transactionId);
            
            // 执行补偿操作
            boolean compensationResult = compensationService.executeCompensation(
                    transactionId, "定时任务补偿：消费失败");
            
            // 更新补偿状态
            if (compensationResult) {
                updateCompensationStatus(transactionId, "SUCCESS", "补偿成功");
                log.info("事务补偿执行成功: transactionId={}", transactionId);
            } else {
                updateCompensationStatus(transactionId, "FAILED", "补偿失败");
                log.error("事务补偿执行失败: transactionId={}", transactionId);
            }
            
        } catch (Exception e) {
            log.error("为事务执行补偿时发生异常: transactionId={}", transactionId, e);
            updateCompensationStatus(transactionId, "ERROR", "补偿异常：" + e.getMessage());
        }
    }

    /**
     * 更新补偿状态
     */
    private void updateCompensationStatus(String transactionId, String status, String message) {
        try {
            // 通过errorMessage字段记录补偿状态
            String fullMessage = message + " [补偿状态:" + status + "] [时间:" + LocalDateTime.now() + "]";
            transactionLogMapper.updateErrorMessage(transactionId, fullMessage);
            
            log.info("更新补偿状态成功: transactionId={}, status={}", transactionId, status);
        } catch (Exception e) {
            log.error("更新补偿状态失败: transactionId={}, status={}", transactionId, status, e);
        }
    }

    /**
     * 定时清理旧的事务日志
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldTransactionLogs() {
        log.info("开始清理旧的事务日志");
        
        try {
            // 删除30天前的事务日志
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(30);
            
            int deletedCount = transactionLogMapper.delete(
                    new LambdaQueryWrapper<RocketmqTransactionLog>()
                            .lt(RocketmqTransactionLog::getCreatedTime, cutoffTime)
            );
            
            log.info("清理旧事务日志完成，删除{}条记录", deletedCount);
            
        } catch (Exception e) {
            log.error("清理旧事务日志时发生异常", e);
        }
    }
} 