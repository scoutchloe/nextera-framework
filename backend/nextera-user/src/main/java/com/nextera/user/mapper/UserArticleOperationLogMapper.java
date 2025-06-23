package com.nextera.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nextera.user.entity.UserArticleOperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 用户文章操作日志Mapper
 *
 * @author nextera
 * @since 2025-06-23
 */
@Mapper
public interface UserArticleOperationLogMapper extends BaseMapper<UserArticleOperationLog> {

    /**
     * 更新操作状态
     *
     * @param transactionId 事务ID
     * @param operationStatus 操作状态
     * @return 更新行数
     */
    @Update("UPDATE user_article_operation_log SET operation_status = #{operationStatus}, updated_time = NOW() WHERE transaction_id = #{transactionId}")
    int updateOperationStatus(@Param("transactionId") String transactionId, @Param("operationStatus") Integer operationStatus);

    /**
     * 更新错误信息
     *
     * @param transactionId 事务ID
     * @param errorMessage 错误信息
     * @return 更新行数
     */
    @Update("UPDATE user_article_operation_log SET error_message = #{errorMessage}, updated_time = NOW() WHERE transaction_id = #{transactionId}")
    int updateErrorMessage(@Param("transactionId") String transactionId, @Param("errorMessage") String errorMessage);
} 