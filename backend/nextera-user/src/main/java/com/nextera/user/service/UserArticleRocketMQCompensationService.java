package com.nextera.user.service;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nextera.user.dto.ArticleUpdateMessageDTO;
import com.nextera.user.entity.RocketmqTransactionLog;
import com.nextera.user.entity.User;
import com.nextera.user.entity.UserArticleOperationLog;
import com.nextera.user.mapper.RocketmqTransactionLogMapper;
import com.nextera.user.mapper.UserArticleOperationLogMapper;
import com.nextera.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * RocketMQ事务补偿服务
 * 处理消费失败时的数据补偿和回滚
 *
 * @author nextera
 * @since 2025-06-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserArticleRocketMQCompensationService {

    private final UserMapper userMapper;
    private final RocketmqTransactionLogMapper transactionLogMapper;
    private final UserArticleOperationLogMapper operationLogMapper;

    /**
     * 执行补偿操作
     * 当消费者处理失败时，回滚本地事务的影响
     *
     * @param transactionId 事务ID
     * @param errorMessage 错误信息
     * @return 补偿结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean executeCompensation(String transactionId, String errorMessage) {
        log.info("开始执行补偿操作: transactionId={}", transactionId);
        
        try {
            // 1. 查询事务日志
            RocketmqTransactionLog transactionLog = transactionLogMapper.selectOne(
                    new LambdaQueryWrapper<RocketmqTransactionLog>()
                            .eq(RocketmqTransactionLog::getTransactionId, transactionId)
            );
            
            if (transactionLog == null) {
                log.warn("未找到事务日志，无法执行补偿: transactionId={}", transactionId);
                return false;
            }
            
            // 2. 解析消息体获取原始数据
            ArticleUpdateMessageDTO messageDTO = JSONUtil.toBean(
                    transactionLog.getMessageBody(), ArticleUpdateMessageDTO.class);
            
            // 3. 查询用户原始的lastLoginTime（从操作日志中恢复）
            boolean rollbackResult = rollbackUserLastLoginTime(messageDTO.getUserId(), transactionId);
            
            if (rollbackResult) {
                // 4. 更新事务状态为已回滚
                updateTransactionStatusToRollback(transactionId, errorMessage);
                
                // 5. 更新操作日志状态为已回滚
                updateOperationLogStatusToRollback(transactionId, errorMessage);
                
                log.info("补偿操作执行成功: transactionId={}", transactionId);
                return true;
            } else {
                log.error("补偿操作执行失败: transactionId={}", transactionId);
                return false;
            }
            
        } catch (Exception e) {
            log.error("补偿操作异常: transactionId={}", transactionId, e);
            return false;
        }
    }

    /**
     * 回滚用户最后登录时间
     * 将用户的lastLoginTime恢复到事务开始前的状态
     */
    private boolean rollbackUserLastLoginTime(Long userId, String transactionId) {
        try {
            log.info("开始回滚用户lastLoginTime: userId={}, transactionId={}", userId, transactionId);
            
            // 查询用户当前信息
            User currentUser = userMapper.selectById(userId);
            if (currentUser == null) {
                log.warn("用户不存在，无法回滚: userId={}", userId);
                return false;
            }
            
            // 查询操作日志中的原始数据
            UserArticleOperationLog operationLog = operationLogMapper.selectOne(
                    new LambdaQueryWrapper<UserArticleOperationLog>()
                            .eq(UserArticleOperationLog::getTransactionId, transactionId)
                            .eq(UserArticleOperationLog::getUserId, userId)
            );
            
            if (operationLog != null && operationLog.getOldData() != null) {
                // 如果有原始数据，恢复到原始状态
                try {
                    log.info("从操作日志恢复用户数据: userId={}", userId);
                    // 解析原始数据
                    Map<String, Object> oldDataMap = JSONUtil.toBean(operationLog.getOldData(), Map.class);
                    Object lastLoginTimeObj = oldDataMap.get("lastLoginTime");
                    
                    if (lastLoginTimeObj != null) {
                        // 如果是字符串格式的时间，需要转换
                        LocalDateTime originalLastLoginTime;
                        if (lastLoginTimeObj instanceof String) {
                            originalLastLoginTime = LocalDateTime.parse((String) lastLoginTimeObj);
                        } else if (lastLoginTimeObj instanceof LocalDateTime) {
                            originalLastLoginTime = (LocalDateTime) lastLoginTimeObj;
                            // LocalDateTime 类型会被转型为Long 类型的时间戳 (13位)
                        } else if (lastLoginTimeObj instanceof Long) {
                            originalLastLoginTime = LocalDateTimeUtil.of((Long)lastLoginTimeObj);
                        } else {
                            // 如果格式不匹配，使用默认策略
                            log.warn("无法解析lastLoginTime格式: {}", lastLoginTimeObj);
                            originalLastLoginTime = LocalDateTime.now().minusHours(1);
                        }
                        
                        currentUser.setLastLoginTime(originalLastLoginTime);
                        log.info("恢复用户lastLoginTime到原始值: userId={}, originalTime={}", 
                                userId, originalLastLoginTime);
                    } else {
                        log.warn("原始数据中未找到lastLoginTime，使用默认回滚策略: userId={}", userId);
                        currentUser.setLastLoginTime(LocalDateTime.now().minusHours(1));
                    }
                } catch (Exception e) {
                    log.warn("解析原始数据失败，使用默认回滚策略: userId={}", userId, e);
                    // 如果解析失败，使用默认回滚策略
                    currentUser.setLastLoginTime(LocalDateTime.now().minusHours(1));
                }
            } else {
                // 如果没有原始数据，使用默认回滚策略
                log.info("无原始数据，使用默认回滚策略: userId={}", userId);
                currentUser.setLastLoginTime(LocalDateTime.now().minusHours(1));
            }
            
            // 更新用户信息
            int updateResult = userMapper.updateById(currentUser);
            if (updateResult > 0) {
                log.info("用户lastLoginTime回滚成功: userId={}", userId);
                return true;
            } else {
                log.error("用户lastLoginTime回滚失败: userId={}", userId);
                return false;
            }
            
        } catch (Exception e) {
            log.error("回滚用户lastLoginTime异常: userId={}", userId, e);
            return false;
        }
    }

    /**
     * 更新事务状态为已回滚
     */
    private void updateTransactionStatusToRollback(String transactionId, String errorMessage) {
        try {
            transactionLogMapper.updateMessageStatus(transactionId, 
                    RocketmqTransactionLog.MessageStatus.ROLLBACK.getCode());
            transactionLogMapper.updateLocalTransactionStatus(transactionId, 
                    RocketmqTransactionLog.LocalTransactionStatus.ROLLBACK.getCode());
            transactionLogMapper.updateErrorMessage(transactionId, "消费失败，已执行补偿: " + errorMessage);
            
            log.info("事务状态更新为已回滚: transactionId={}", transactionId);
        } catch (Exception e) {
            log.error("更新事务状态失败: transactionId={}", transactionId, e);
        }
    }

    /**
     * 更新操作日志状态为已回滚
     */
    private void updateOperationLogStatusToRollback(String transactionId, String errorMessage) {
        try {
            operationLogMapper.updateOperationStatus(transactionId, 
                    UserArticleOperationLog.OperationStatus.FAILED.getCode());
            operationLogMapper.updateErrorMessage(transactionId, "消费失败，已执行补偿: " + errorMessage);
            
            log.info("操作日志状态更新为已回滚: transactionId={}", transactionId);
        } catch (Exception e) {
            log.error("更新操作日志状态失败: transactionId={}", transactionId, e);
        }
    }

    /**
     * 检查是否需要补偿
     * 根据事务状态判断是否需要执行补偿操作
     */
    public boolean needsCompensation(String transactionId) {
        try {
            RocketmqTransactionLog transactionLog = transactionLogMapper.selectOne(
                    new LambdaQueryWrapper<RocketmqTransactionLog>()
                            .eq(RocketmqTransactionLog::getTransactionId, transactionId)
            );
            
            if (transactionLog == null) {
                return false;
            }
            
            // 如果本地事务已提交但消息状态不是已提交，说明需要补偿
            boolean localCommitted = RocketmqTransactionLog.LocalTransactionStatus.COMMITTED.getCode()
                    .equals(transactionLog.getLocalTransactionStatus());
            boolean messageCommitted = RocketmqTransactionLog.MessageStatus.COMMITTED.getCode()
                    .equals(transactionLog.getMessageStatus());
            
            return localCommitted && !messageCommitted;
            
        } catch (Exception e) {
            log.error("检查补偿需求失败: transactionId={}", transactionId, e);
            return false;
        }
    }
} 