package com.nextera.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nextera.user.entity.RocketmqTransactionLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * RocketMQ事务日志Mapper
 *
 * @author nextera
 * @since 2025-06-23
 */
@Mapper
public interface RocketmqTransactionLogMapper extends BaseMapper<RocketmqTransactionLog> {

    /**
     * 更新消息状态
     *
     * @param transactionId 事务ID
     * @param messageStatus 消息状态
     * @return 更新行数
     */
    @Update("UPDATE rocketmq_transaction_log SET message_status = #{messageStatus}, updated_time = NOW() WHERE transaction_id = #{transactionId}")
    int updateMessageStatus(@Param("transactionId") String transactionId, @Param("messageStatus") Integer messageStatus);

    /**
     * 更新本地事务状态
     *
     * @param transactionId 事务ID
     * @param localTransactionStatus 本地事务状态
     * @return 更新行数
     */
    @Update("UPDATE rocketmq_transaction_log SET local_transaction_status = #{localTransactionStatus}, updated_time = NOW() WHERE transaction_id = #{transactionId}")
    int updateLocalTransactionStatus(@Param("transactionId") String transactionId, @Param("localTransactionStatus") Integer localTransactionStatus);

    /**
     * 增加重试次数
     *
     * @param transactionId 事务ID
     * @return 更新行数
     */
    @Update("UPDATE rocketmq_transaction_log SET retry_count = retry_count + 1, updated_time = NOW() WHERE transaction_id = #{transactionId}")
    int incrementRetryCount(@Param("transactionId") String transactionId);

    /**
     * 更新错误信息
     *
     * @param transactionId 事务ID
     * @param errorMessage 错误信息
     * @return 更新行数
     */
    @Update("UPDATE rocketmq_transaction_log SET error_message = #{errorMessage}, updated_time = NOW() WHERE transaction_id = #{transactionId}")
    int updateErrorMessage(@Param("transactionId") String transactionId, @Param("errorMessage") String errorMessage);

    /**
     * 根据transactionId 查询事务消息.
     *
     * @param transactionId
     * @return RocketmqTransactionLog
     */
    @Select("SELECT * from rocketmq_transaction_log where transaction_id = #{transactionId} ")
    RocketmqTransactionLog selectByTransactionId(@Param("transactionId") String transactionId);
} 