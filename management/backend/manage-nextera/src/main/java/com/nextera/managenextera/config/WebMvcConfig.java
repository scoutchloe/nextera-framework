package com.nextera.managenextera.config;

import com.nextera.managenextera.interceptor.AntiReplayInterceptor;
import com.nextera.managenextera.interceptor.SignatureInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private SignatureInterceptor signatureInterceptor;
    
    @Autowired
    private AntiReplayInterceptor antiReplayInterceptor;

    /**
     * 配置拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        log.info("配置拦截器 - 开始注册拦截器");
        
        // 注释掉签名验证拦截器，改用过滤器处理
        /*
        registry.addInterceptor(signatureInterceptor)
                .addPathPatterns("/system/role", "/system/role/") // 修正路径，移除api前缀
                .order(1); // 设置较高优先级
        
        log.info("已注册签名验证拦截器: {} (order=1)", signatureInterceptor.getClass().getSimpleName());
        log.info("签名验证拦截器目标路径: /system/role, /system/role/");
        */
        log.info("签名验证改用过滤器处理，跳过拦截器注册");
        
        // 添加防重放攻击拦截器，设置较低优先级
        registry.addInterceptor(antiReplayInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                    "/error",
                    "/swagger-ui/**",
                    "/swagger-resources/**",
                    "/v3/api-docs/**",
                    "/webjars/**",
                    "/doc.html",
                    "/favicon.ico",
                    "/static/**",
                    "/system/role", // 排除签名验证接口，由过滤器处理
                    "/system/role/"
                )
                .order(10); // 设置较低优先级
        
        log.info("已注册防重放拦截器: {} (order=10)", antiReplayInterceptor.getClass().getSimpleName());
    }

    /**
     * 配置跨域
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 配置静态资源处理
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
} 