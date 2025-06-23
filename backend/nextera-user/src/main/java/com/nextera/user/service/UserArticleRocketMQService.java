package com.nextera.user.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nextera.api.article.dto.ArticleCreateRequest;
import com.nextera.common.core.Result;
import com.nextera.common.util.IpUtil;
import com.nextera.user.config.RocketMQConfig;
import com.nextera.user.dto.ArticleUpdateMessageDTO;
import com.nextera.user.entity.RocketmqTransactionLog;
import com.nextera.user.entity.User;
import com.nextera.user.entity.UserArticleOperationLog;
import com.nextera.user.mapper.RocketmqTransactionLogMapper;
import com.nextera.user.mapper.UserArticleOperationLogMapper;
import com.nextera.user.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于RocketMQ事务消息的用户文章服务
 * 专注于RocketMQ事务消息的发送，事务监听器逻辑已分离到UserArticleRocketMQNonTxService
 *
 * @author nextera
 * @since 2025-06-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserArticleRocketMQService {

    private final RocketMQTemplate rocketMQTemplate;
    private final RocketmqTransactionLogMapper transactionLogMapper;
    private final UserArticleOperationLogMapper operationLogMapper;
    private final UserMapper userMapper;

    /**
     * 使用RocketMQ事务消息更新文章
     * 专注于消息发送，本地事务执行由专门的UserArticleRocketMQNonTxService处理
     *
     * @param articleId 文章ID
     * @param request 文章更新请求
     * @param userId 用户ID
     * @param httpRequest HTTP请求
     * @return 更新结果
     */
    public Result<Boolean> updateArticleWithRocketMQ(Long articleId, ArticleCreateRequest request, 
                                                     Long userId, HttpServletRequest httpRequest) {
        
        String transactionId = generateTransactionId(userId, articleId);
        log.info("开始RocketMQ事务消息处理文章更新: transactionId={}, articleId={}, userId={}", 
                transactionId, articleId, userId);
        
        try {
            // 1. 构建消息体
            ArticleUpdateMessageDTO messageDTO = buildMessageDTO(transactionId, articleId, request, userId, httpRequest);
            
            // 2. 构建RocketMQ消息
            Message<ArticleUpdateMessageDTO> message = MessageBuilder
                    .withPayload(messageDTO)
                    .setHeader("transactionId", transactionId)
                    .setHeader("userId", userId)
                    .setHeader("articleId", articleId)
                    .build();
            
            // 3. 发送事务消息（RocketMQ会先发送半消息，然后调用本地事务监听器）
            String destination = RocketMQConfig.Topics.ARTICLE_UPDATE_TOPIC + ":" + RocketMQConfig.Tags.ARTICLE_UPDATE_TAG;
            log.info("发送RocketMQ事务消息: destination={}, transactionId={}", destination, transactionId);
            
            // 发送事务消息，arg参数会传递给事务监听器（由UserArticleRocketMQNonTxService处理）
            rocketMQTemplate.sendMessageInTransaction(
                    destination,
                    message,
                    messageDTO // 作为arg参数传递给TransactionListener
            );
            
            log.info("RocketMQ事务消息发送成功: transactionId={}", transactionId);
            return Result.success(true);
            
        } catch (Exception e) {
            log.error("RocketMQ事务消息处理失败: transactionId={}, articleId={}, userId={}", 
                    transactionId, articleId, userId, e);
            return Result.error("更新文章失败: " + e.getMessage());
        }
    }

    /**
     * 生成事务ID
     */
    private String generateTransactionId(Long userId, Long articleId) {
        return String.format("TXN_%s_%s_%s", userId, articleId, IdUtil.getSnowflakeNextIdStr());
    }

    /**
     * 构建消息DTO
     */
    private ArticleUpdateMessageDTO buildMessageDTO(String transactionId, Long articleId, 
                                                   ArticleCreateRequest request, Long userId, 
                                                   HttpServletRequest httpRequest) {
        return new ArticleUpdateMessageDTO()
                .setTransactionId(transactionId)
                .setUserId(userId)
                .setArticleId(articleId)
                .setTitle(request.getTitle())
                .setContent(request.getContent())
                .setCategoryId(request.getCategoryId())
                .setSummary(request.getSummary())
                .setTags(request.getTags())
                .setOperationType("UPDATE")
                .setClientIp(IpUtil.getClientIpAddress())
                .setUserAgent(httpRequest.getHeader("User-Agent"))
                .setTimestamp(System.currentTimeMillis());
    }

    /**
     * 执行RocketMQ本地事务
     * 这个方法会被事务监听器调用，需要排除Seata管理
     *
     * @param messageDTO 消息DTO
     * @return 执行结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean executeRocketMQLocalTransaction(ArticleUpdateMessageDTO messageDTO) {
        String transactionId = messageDTO.getTransactionId();
        Long userId = messageDTO.getUserId();
        Long articleId = messageDTO.getArticleId();
        
        log.info("=== 开始执行RocketMQ本地事务 ===");
        log.info("transactionId: {}, userId: {}, articleId: {}", transactionId, userId, articleId);
        
        try {
            // 1. 记录事务日志
            log.info("步骤1: 保存事务日志");
            saveTransactionLog(messageDTO);

            // 获得用户的登录时间.
            LocalDateTime userOldLastLoginTime = getUserOldLastLoginTime(messageDTO.getUserId());
            if (userOldLastLoginTime != null) {
                messageDTO.setLastLoginTime(userOldLastLoginTime);
            }
            // 2. 记录操作日志
            log.info("步骤2: 保存操作日志");
            saveOperationLog(messageDTO);
            
            // 3. 执行业务操作 - 更新用户最后登录时间
            log.info("步骤3: 执行业务操作");
            updateUserLastLoginTime(userId);
            
            // 4. 更新事务状态为已提交
            log.info("步骤4: 更新事务状态为已提交");
            updateTransactionStatus(transactionId, 
                    RocketmqTransactionLog.LocalTransactionStatus.COMMITTED.getCode());
            
            log.info("=== RocketMQ本地事务执行成功: {} ===", transactionId);
            return true;
            
        } catch (Exception e) {
            log.error("=== RocketMQ本地事务执行失败: {} ===", transactionId, e);
            
            // 记录失败状态
            try {
                updateTransactionStatus(transactionId, 
                        RocketmqTransactionLog.LocalTransactionStatus.ROLLBACK.getCode());
                updateTransactionErrorMessage(transactionId, e.getMessage());
            } catch (Exception updateException) {
                log.error("更新事务失败状态异常: transactionId={}", transactionId, updateException);
            }
            
            return false;
        }
    }

    /**
     * 检查RocketMQ本地事务状态
     * 这个方法会被事务监听器调用进行回查
     *
     * @param transactionId 事务ID
     * @return 是否已提交
     */
    public boolean checkRocketMQLocalTransaction(String transactionId) {
        log.info("=== RocketMQ回查本地事务状态 ===");
        log.info("transactionId: {}", transactionId);
        
        try {
            RocketmqTransactionLog transactionLog = transactionLogMapper.selectOne(
                    new LambdaQueryWrapper<RocketmqTransactionLog>()
                            .eq(RocketmqTransactionLog::getTransactionId, transactionId)
            );
            
            if (transactionLog == null) {
                log.warn("未找到事务日志，判断为未提交: transactionId={}", transactionId);
                return false;
            }
            
            boolean isCommitted = RocketmqTransactionLog.LocalTransactionStatus.COMMITTED.getCode()
                    .equals(transactionLog.getLocalTransactionStatus());
            
            log.info("=== 本地事务状态检查完成 ===");
            log.info("transactionId: {}, status: {}, isCommitted: {}", 
                    transactionId, transactionLog.getLocalTransactionStatus(), isCommitted);
            
            return isCommitted;
            
        } catch (Exception e) {
            log.error("检查本地事务状态异常: transactionId={}", transactionId, e);
            return false;
        }
    }

    // ==================== 私有辅助方法 ====================
    
    /**
     * 保存事务日志
     */
    private void saveTransactionLog(ArticleUpdateMessageDTO messageDTO) {
        RocketmqTransactionLog transactionLog = new RocketmqTransactionLog()
                .setTransactionId(messageDTO.getTransactionId())
                .setMessageKey(messageDTO.getTransactionId())
                .setTopic(RocketMQConfig.Topics.ARTICLE_UPDATE_TOPIC)
                .setTag(RocketMQConfig.Tags.ARTICLE_UPDATE_TAG)
                .setMessageBody(JSONUtil.toJsonStr(messageDTO))
                .setMessageStatus(RocketmqTransactionLog.MessageStatus.PREPARING.getCode())
                .setRetryCount(0)
                .setMaxRetryCount(3)
                .setBusinessType("UPDATE_ARTICLE")
                .setBusinessId(messageDTO.getArticleId().toString())
                .setUserId(messageDTO.getUserId())
                .setArticleId(messageDTO.getArticleId())
                .setCreatedTime(LocalDateTime.now())
                .setUpdatedTime(LocalDateTime.now())
                .setLocalTransactionStatus(RocketmqTransactionLog.LocalTransactionStatus.PREPARING.getCode())
                .setCreatedBy("rocketmq-system")
                .setUpdatedBy("rocketmq-system");
        
        int result = transactionLogMapper.insert(transactionLog);
        if (result <= 0) {
            throw new RuntimeException("保存事务日志失败: " + messageDTO.getTransactionId());
        }
        
        log.info("保存事务日志成功: transactionId={}, id={}", 
                messageDTO.getTransactionId(), transactionLog.getId());
    }

    /**
     * 保存操作日志
     */
    private void saveOperationLog(ArticleUpdateMessageDTO messageDTO) {
        // 构建操作详情JSON对象
        Map<String, Object> operationDetailsMap = new HashMap<>();
        operationDetailsMap.put("description", String.format("用户%d通过RocketMQ更新文章%d: %s", 
                messageDTO.getUserId(), messageDTO.getArticleId(), messageDTO.getTitle()));
        operationDetailsMap.put("operationType", "UPDATE_ARTICLE_ROCKETMQ");
        operationDetailsMap.put("articleTitle", messageDTO.getTitle());
        operationDetailsMap.put("articleContent", messageDTO.getContent());
        operationDetailsMap.put("timestamp", messageDTO.getTimestamp());
        String operationDetailsJson = JSONUtil.toJsonStr(operationDetailsMap);
        
        UserArticleOperationLog operationLog = new UserArticleOperationLog()
                .setTransactionId(messageDTO.getTransactionId())
                .setUserId(messageDTO.getUserId())
                .setArticleId(messageDTO.getArticleId())
                .setOperationType("UPDATE_ARTICLE_ROCKETMQ")
                .setOperationStatus(UserArticleOperationLog.OperationStatus.IN_PROGRESS.getCode())
                .setNewData(operationDetailsJson)  // 存储JSON格式的操作详情
                .setClientIp(messageDTO.getClientIp())
                .setUserAgent(messageDTO.getUserAgent())
                .setCreatedTime(LocalDateTime.now())
                .setUpdatedTime(LocalDateTime.now());

        // 设置用户旧的更新时间值
        if (messageDTO.getLastLoginTime() != null) {
            Map<String, Object> oldDataMap = new HashMap<>();
            oldDataMap.put("lastLoginTime", messageDTO.getLastLoginTime());
            operationLog.setOldData(JSONUtil.toJsonStr(oldDataMap));
        }
        
        int result = operationLogMapper.insert(operationLog);
        if (result <= 0) {
            throw new RuntimeException("保存操作日志失败: " + messageDTO.getTransactionId());
        }
        
        log.info("保存操作日志成功: transactionId={}, id={}", 
                messageDTO.getTransactionId(), operationLog.getId());
    }


    private LocalDateTime getUserOldLastLoginTime(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        return user.getLastLoginTime();
    }

    /**
     * 更新用户最后登录时间
     */
    private void updateUserLastLoginTime(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在: " + userId);
        }
        
        LocalDateTime now = LocalDateTime.now();
        user.setLastLoginTime(now);
        user.setUpdateTime(now);
        
        int result = userMapper.updateById(user);
        if (result <= 0) {
            throw new RuntimeException("更新用户最后登录时间失败: " + userId);
        }
        
        log.info("更新用户最后登录时间成功: userId={}, lastLoginTime={}", userId, now);
    }

    /**
     * 更新事务状态
     */
    private void updateTransactionStatus(String transactionId, Integer status) {
        int result = transactionLogMapper.updateLocalTransactionStatus(transactionId, status);
        if (result <= 0) {
            throw new RuntimeException("更新事务状态失败: " + transactionId);
        }
        log.info("更新事务状态成功: transactionId={}, status={}", transactionId, status);
    }

    /**
     * 更新事务错误信息
     */
    private void updateTransactionErrorMessage(String transactionId, String errorMessage) {
        int result = transactionLogMapper.updateErrorMessage(transactionId, errorMessage);
        if (result <= 0) {
            log.warn("更新事务错误信息失败: transactionId={}", transactionId);
        }
        log.info("更新事务错误信息成功: transactionId={}", transactionId);
    }
} 