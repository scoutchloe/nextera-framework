package com.nextera.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 用户文章操作日志实体
 * 用于记录用户对文章的操作历史
 *
 * @author nextera
 * @since 2025-06-23
 */
@Data
@Accessors(chain = true)
@TableName("user_article_operation_log")
public class UserArticleOperationLog {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 操作类型：CREATE, UPDATE, DELETE
     */
    private String operationType;

    /**
     * 操作状态：0-进行中，1-成功，2-失败
     */
    private Integer operationStatus;

    /**
     * 关联的事务ID
     */
    private String transactionId;

    /**
     * 操作前的数据（JSON格式）
     */
    private String oldData;

    /**
     * 操作后的数据（JSON格式）
     */
    private String newData;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 用户代理
     */
    private String userAgent;

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
     * 操作类型枚举
     */
    public enum OperationType {
        CREATE("CREATE", "创建"),
        UPDATE("UPDATE", "更新"),
        DELETE("DELETE", "删除");

        private final String code;
        private final String desc;

        OperationType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public String getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    /**
     * 操作状态枚举
     */
    public enum OperationStatus {
        IN_PROGRESS(0, "进行中"),
        SUCCESS(1, "成功"),
        FAILED(2, "失败");

        private final Integer code;
        private final String desc;

        OperationStatus(Integer code, String desc) {
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