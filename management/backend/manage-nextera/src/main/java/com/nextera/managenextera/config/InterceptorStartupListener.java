package com.nextera.managenextera.config;

import com.nextera.managenextera.interceptor.SignatureInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 拦截器启动监听器
 * 用于验证拦截器是否正确注册和初始化
 */
@Slf4j
@Component
public class InterceptorStartupListener implements ApplicationRunner {

    @Autowired
    private SignatureInterceptor signatureInterceptor;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("=== 拦截器启动检查 ===");
        log.info("SignatureInterceptor实例: {}", signatureInterceptor != null ? "已注入" : "未注入");
        
        if (signatureInterceptor != null) {
            log.info("SignatureInterceptor类: {}", signatureInterceptor.getClass().getName());
        }
        
        log.info("拦截器目标路径: POST /system/role");
        log.info("签名头部: X-Signature, X-Timestamp");
        log.info("=== 拦截器启动检查完成 ===");
    }
} 