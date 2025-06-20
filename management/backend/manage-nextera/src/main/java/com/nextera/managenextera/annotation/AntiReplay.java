package com.nextera.managenextera.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 防重放攻击注解
 * 用于标记需要防重放保护的接口
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AntiReplay {
    
    /**
     * 时间窗口（毫秒），默认5分钟
     */
    long timeWindow() default 5 * 60 * 1000;
    
    /**
     * 是否启用nonce验证，默认启用
     */
    boolean enableNonce() default true;
    
    /**
     * 是否启用序列号验证，默认不启用
     */
    boolean enableSequence() default false;
    
    /**
     * 是否启用签名验证，默认不启用
     */
    boolean enableSignature() default false;
    
    /**
     * 密钥名称，用于从配置中获取密钥
     * 默认为 "anti-replay.secret-key"
     */
    String secretKeyName() default "anti-replay.secret-key";
} 