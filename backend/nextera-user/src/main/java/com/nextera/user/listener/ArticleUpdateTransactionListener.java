package com.nextera.user.listener;

import com.nextera.user.dto.ArticleUpdateMessageDTO;
import com.nextera.user.service.UserArticleRocketMQService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * 文章更新事务监听器
 * 处理RocketMQ事务消息的本地事务执行和回查
 * 使用专门的非Seata服务，避免事务代理冲突
 *
 * @author nextera
 * @since 2025-06-23
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQTransactionListener
public class ArticleUpdateTransactionListener implements RocketMQLocalTransactionListener {

    private final UserArticleRocketMQService userArticleRocketMQService;

    /**
     * 执行本地事务
     * 当RocketMQ发送半消息成功后，会回调此方法执行本地事务
     * 使用专门的非Seata服务，避免branchType为null的错误
     *
     * @param message 消息内容
     * @param arg 发送消息时传递的参数
     * @return 本地事务执行状态
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object arg) {
        String transactionId = (String) message.getHeaders().get("transactionId");
        log.info("执行本地事务开始(使用非Seata服务): transactionId={}", transactionId);
        
        try {
            // arg是发送消息时传递的参数，这里是ArticleUpdateMessageDTO
            ArticleUpdateMessageDTO messageDTO = (ArticleUpdateMessageDTO) arg;
            
            // 使用专门的非Seata服务执行本地事务
            boolean success = userArticleRocketMQService.executeRocketMQLocalTransaction(messageDTO);
            
            if (success) {
                log.info("本地事务执行成功，提交消息(非Seata): transactionId={}", transactionId);
                return RocketMQLocalTransactionState.COMMIT;
            } else {
                log.error("本地事务执行失败，回滚消息(非Seata): transactionId={}", transactionId);
                return RocketMQLocalTransactionState.ROLLBACK;
            }
            
        } catch (Exception e) {
            log.error("本地事务执行异常，回滚消息(非Seata): transactionId={}", transactionId, e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    /**
     * 检查本地事务状态
     * 当RocketMQ需要回查本地事务状态时，会回调此方法
     * 使用专门的非Seata服务，避免branchType为null的错误
     * 
     * 回查场景：
     * 1. 本地事务执行超时
     * 2. 网络异常导致事务状态丢失
     * 3. RocketMQ定期回查
     *
     * @param message 消息内容
     * @return 本地事务状态
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        String transactionId = (String) message.getHeaders().get("transactionId");
        log.info("RocketMQ回查本地事务状态(使用非Seata服务): transactionId={}", transactionId);
        
        try {
            // 使用专门的非Seata服务检查本地事务状态
            boolean isCommitted = userArticleRocketMQService.checkRocketMQLocalTransaction(transactionId);
            
            if (isCommitted) {
                log.info("回查结果：本地事务已提交，提交消息(非Seata): transactionId={}", transactionId);
                return RocketMQLocalTransactionState.COMMIT;
            } else {
                log.info("回查结果：本地事务未提交，回滚消息(非Seata): transactionId={}", transactionId);
                return RocketMQLocalTransactionState.ROLLBACK;
            }
            
        } catch (Exception e) {
            log.error("回查本地事务状态异常(非Seata): transactionId={}", transactionId, e);
            // 回查异常时，可以选择UNKNOWN让RocketMQ继续回查，或者ROLLBACK直接回滚
            return RocketMQLocalTransactionState.UNKNOWN;
        }
    }
} 