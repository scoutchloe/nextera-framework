package com.nextera.managenextera.config;

import com.nextera.managenextera.filter.HybridEncryptionFilter;
import com.nextera.managenextera.filter.SignatureVerificationFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 过滤器配置
 */
@Slf4j
@Configuration
public class FilterConfig {

    /**
     * 注册签名验证过滤器
     */
    @Bean
    public FilterRegistrationBean<SignatureVerificationFilter> signatureVerificationFilter() {
        FilterRegistrationBean<SignatureVerificationFilter> registrationBean = new FilterRegistrationBean<>();
        
        registrationBean.setFilter(new SignatureVerificationFilter());
        registrationBean.addUrlPatterns("/system/role", "/api/system/role");
        registrationBean.setOrder(1); // 设置较高优先级
        registrationBean.setName("signatureVerificationFilter");
        
        log.info("已注册签名验证过滤器: SignatureVerificationFilter (order=1)");
        log.info("签名验证过滤器目标路径: /system/role, /api/system/role");
        
        return registrationBean;
    }

    /**
     * 注册混合加密解密过滤器
     */
    @Bean
    public FilterRegistrationBean<HybridEncryptionFilter> hybridEncryptionFilter() {
        FilterRegistrationBean<HybridEncryptionFilter> registrationBean = new FilterRegistrationBean<>();
        
        registrationBean.setFilter(new HybridEncryptionFilter());
        registrationBean.addUrlPatterns("/system/role", "/api/system/role");
        registrationBean.setOrder(2); // 在签名验证过滤器之后执行
        registrationBean.setName("hybridEncryptionFilter");
        
        log.info("已注册混合加密解密过滤器: HybridEncryptionFilter (order=2)");
        log.info("混合加密解密过滤器目标路径: /system/role, /api/system/role (仅PUT方法)");
        
        return registrationBean;
    }
} 