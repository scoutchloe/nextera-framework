package com.nextera.user.service.tcc;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * TCC操作状态
 * 用于解决TCC的幂等、空回滚、悬挂问题
 *
 * @author Scout
 * @date 2025-06-18
 * @since 1.0
 */
@Data
public class TccActionState {

    /**
     * 事务ID
     */
    private String xid;

    /**
     * 分支事务ID
     */
    private Long branchId;

    /**
     * 动作名称
     */
    private String actionName;

    /**
     * 状态：INIT-初始状态，TRIED-已尝试，COMMITTED-已提交，ROLLBACKED-已回滚
     */
    private TccStatus status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    private String businessKey;

    /**
     * 业务数据（JSON格式）
     */
    private String businessData;

    /**
     * 原始数据备份（用于回滚）
     */
    private String originalData;

    /**
     * 重试次数
     */
    private Integer retryCount = 0;

    /**
     * 最大重试次数（可配置）
     */
    private Integer maxRetryCount = 3;

    public enum TccStatus {
        INIT, TRIED, COMMITTED, ROLLBACKED, FAILED
    }

    /**
     * 检查是否可以重试
     */
    public boolean canRetry() {
        return retryCount < maxRetryCount;
    }

    /**
     * 增加重试次数
     */
    public void incrementRetryCount() {
        this.retryCount++;
    }

    /**
     * 设置最大重试次数
     */
    public void setMaxRetryCount(Integer maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    /**
     * 获取最大重试次数
     */
    public Integer getMaxRetryCount() {
        return maxRetryCount;
    }
} 