package com.nextera.user.listener;

import com.nextera.api.article.dto.ArticleCreateRequest;
import com.nextera.common.core.Result;
import com.nextera.user.client.ArticleServiceClient;
import com.nextera.user.config.RocketMQConfig;
import com.nextera.user.dto.ArticleUpdateMessageDTO;
import com.nextera.user.entity.RocketmqTransactionLog;
import com.nextera.user.entity.UserArticleOperationLog;
import com.nextera.user.mapper.RocketmqTransactionLogMapper;
import com.nextera.user.mapper.UserArticleOperationLogMapper;
import com.nextera.user.service.UserArticleRocketMQCompensationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 文章更新消息消费者
 * 消费RocketMQ事务消息，调用文章服务更新文章
 *
 * @author nextera
 * @since 2025-06-23
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
    topic = RocketMQConfig.Topics.ARTICLE_UPDATE_TOPIC,
    selectorExpression = RocketMQConfig.Tags.ARTICLE_UPDATE_TAG,
    consumerGroup = RocketMQConfig.ConsumerGroups.ARTICLE_CONSUMER_GROUP
)
public class ArticleUpdateMessageConsumer implements RocketMQListener<ArticleUpdateMessageDTO> {

    private final ArticleServiceClient articleServiceClient;
    private final RocketmqTransactionLogMapper transactionLogMapper;
    private final UserArticleOperationLogMapper operationLogMapper;
    private final UserArticleRocketMQCompensationService compensationService;

    @Override
    public void onMessage(ArticleUpdateMessageDTO messageDTO) {
        String transactionId = messageDTO.getTransactionId();
        Long articleId = messageDTO.getArticleId();
        Long userId = messageDTO.getUserId();
        
        log.info("接收到文章更新消息: transactionId={}, articleId={}, userId={}", 
                transactionId, articleId, userId);
        
        try {
            // 1. 更新消息状态为已提交
            updateMessageStatus(transactionId, RocketmqTransactionLog.MessageStatus.COMMITTED.getCode());
            
            // 2. 调用文章服务更新文章
            updateArticleViaDubbo(messageDTO);
            
            // 3. 更新操作日志状态为成功
            updateOperationStatus(transactionId, UserArticleOperationLog.OperationStatus.SUCCESS.getCode());
            
            log.info("文章更新消息处理成功: transactionId={}, articleId={}", transactionId, articleId);
            
        } catch (Exception e) {
            log.error("文章更新消息处理失败: transactionId={}, articleId={}", transactionId, articleId, e);
            
            // 执行补偿操作 - 回滚用户的lastLoginTime
            try {
                log.info("开始执行补偿操作: transactionId={}", transactionId);
                boolean compensationResult = compensationService.executeCompensation(transactionId, e.getMessage());
                
                if (compensationResult) {
                    log.info("补偿操作执行成功: transactionId={}", transactionId);
                } else {
                    log.error("补偿操作执行失败: transactionId={}", transactionId);
                }
            } catch (Exception compensationException) {
                log.error("补偿操作异常: transactionId={}", transactionId, compensationException);
            }
            
            // 更新消息状态为已回滚
            updateMessageStatus(transactionId, RocketmqTransactionLog.MessageStatus.ROLLBACK.getCode());
            
            // 更新操作日志状态为失败
            updateOperationStatus(transactionId, UserArticleOperationLog.OperationStatus.FAILED.getCode());
            updateOperationErrorMessage(transactionId, e.getMessage());
            
            // 重新抛出异常，让RocketMQ进行重试
            throw new RuntimeException("文章更新失败", e);
        }
    }

    /**
     * 通过非事务调用器调用文章服务更新文章
     */
    private void updateArticleViaDubbo(ArticleUpdateMessageDTO messageDTO) {
        try {
            log.info("开始调用文章服务更新文章: articleId={}, title={}", 
                    messageDTO.getArticleId(), messageDTO.getTitle());
            
            // 构建文章更新请求
            ArticleCreateRequest updateRequest = buildArticleUpdateRequest(messageDTO);
            
            // 使用编程式配置的Dubbo调用器，完全绕过Seata避免branchType为null错误
            Result<Boolean> result = articleServiceClient.updateArticleForRocketMQProgrammatic(
                    messageDTO.getArticleId(), 
                    updateRequest,
                    messageDTO.getUserId(), 
                    "rocketmq", // 标识来源
                    messageDTO.getClientIp(), 
                    messageDTO.getUserAgent()
            );
            
            if (result == null) {
                throw new RuntimeException("文章服务返回null");
            }
            
            if (!result.isSuccess()) {
                throw new RuntimeException("文章服务返回失败: " + result.getMessage());
            }
            
            log.info("文章服务调用成功: articleId={}, result={}", messageDTO.getArticleId(), result.getData());
            
        } catch (Exception e) {
            log.error("调用文章服务失败: articleId={}", messageDTO.getArticleId(), e);
            throw new RuntimeException("调用文章服务失败", e);
        }
    }

    /**
     * 更新消息状态
     */
    private void updateMessageStatus(String transactionId, Integer messageStatus) {
        try {
            transactionLogMapper.updateMessageStatus(transactionId, messageStatus);
            log.info("更新消息状态成功: transactionId={}, status={}", transactionId, messageStatus);
        } catch (Exception e) {
            log.error("更新消息状态失败: transactionId={}, status={}", transactionId, messageStatus, e);
        }
    }

    /**
     * 更新操作状态
     */
    private void updateOperationStatus(String transactionId, Integer operationStatus) {
        try {
            operationLogMapper.updateOperationStatus(transactionId, operationStatus);
            log.info("更新操作状态成功: transactionId={}, status={}", transactionId, operationStatus);
        } catch (Exception e) {
            log.error("更新操作状态失败: transactionId={}, status={}", transactionId, operationStatus, e);
        }
    }

    /**
     * 更新操作错误信息
     */
    private void updateOperationErrorMessage(String transactionId, String errorMessage) {
        try {
            operationLogMapper.updateErrorMessage(transactionId, errorMessage);
            log.info("更新操作错误信息成功: transactionId={}", transactionId);
        } catch (Exception e) {
            log.error("更新操作错误信息失败: transactionId={}", transactionId, e);
        }
    }

    /**
     * 构建文章更新请求（示例方法，实际使用时需要根据文章服务接口调整）
     */
     private ArticleCreateRequest buildArticleUpdateRequest(ArticleUpdateMessageDTO messageDTO) {
         ArticleCreateRequest request = new ArticleCreateRequest();
         request.setTitle(messageDTO.getTitle());
         request.setContent(messageDTO.getContent());
         request.setCategoryId(messageDTO.getCategoryId());
         request.setSummary(messageDTO.getSummary());
         request.setTags(messageDTO.getTags());
         return request;
     }
} 