package com.nextera.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * TCC重试控制配置
 * 
 * @author Scout
 * @date 2025-06-18
 * @since 1.0
 */
@Configuration
@ConfigurationProperties(prefix = "tcc.retry")
public class TccRetryConfiguration {

    /**
     * 最大重试次数，默认3次
     */
    private int maxRetryCount = 3;

    /**
     * 重试间隔时间（毫秒），默认1秒
     */
    private long retryInterval = 1000;

    /**
     * 是否启用重试控制，默认启用
     */
    private boolean enabled = true;

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public long getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(long retryInterval) {
        this.retryInterval = retryInterval;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
} 