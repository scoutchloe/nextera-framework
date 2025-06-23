package com.nextera.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * RocketMQ事务消息日志实体
 * 用于记录和管理RocketMQ事务消息的状态
 *
 * @author nextera
 * @since 2025-06-23
 */
@Data
@Accessors(chain = true)
@TableName("rocketmq_transaction_log")
public class RocketmqTransactionLog {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 事务ID，业务唯一标识
     */
    private String transactionId;

    /**
     * 消息Key
     */
    private String messageKey;

    /**
     * RocketMQ Topic
     */
    private String topic;

    /**
     * RocketMQ Tag
     */
    private String tag;

    /**
     * 消息体内容
     */
    private String messageBody;

    /**
     * 消息状态：0-准备中，1-已提交，2-已回滚
     */
    private Integer messageStatus;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount;

    /**
     * 业务类型：UPDATE_ARTICLE等
     */
    private String businessType;

    /**
     * 业务ID
     */
    private String businessId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 本地事务状态：0-准备中，1-已提交，2-已回滚
     */
    private Integer localTransactionStatus;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createdBy;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updatedBy;

    /**
     * 消息状态枚举
     */
    public enum MessageStatus {
        PREPARING(0, "准备中"),
        COMMITTED(1, "已提交"),
        ROLLBACK(2, "已回滚");

        private final Integer code;
        private final String desc;

        MessageStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 本地事务状态枚举
     */
    public enum LocalTransactionStatus {
        PREPARING(0, "准备中"),
        COMMITTED(1, "已提交"),
        ROLLBACK(2, "已回滚");

        private final Integer code;
        private final String desc;

        LocalTransactionStatus(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }
} 