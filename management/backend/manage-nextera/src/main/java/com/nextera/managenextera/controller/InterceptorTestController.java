package com.nextera.managenextera.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 拦截器测试控制器
 * 用于测试拦截器配置是否正确
 */
@Slf4j
@RestController
@RequestMapping("/interceptor-test")
public class InterceptorTestController {

    @GetMapping("/info")
    public Map<String, Object> getInterceptorInfo() {
        log.info("InterceptorTest: 收到拦截器信息查询请求");
        return Map.of(
            "message", "拦截器测试接口",
            "signatureInterceptor", "应拦截 POST /system/role",
            "testEndpoint", "POST /interceptor-test/signature"
        );
    }

    @PostMapping("/signature")
    public Map<String, Object> testSignature(@RequestBody Map<String, Object> data, HttpServletRequest request) {
        log.info("InterceptorTest: 收到签名测试请求");
        log.info("请求URI: {}", request.getRequestURI());
        log.info("请求方法: {}", request.getMethod());
        
        String signature = request.getHeader("X-Signature");
        String timestamp = request.getHeader("X-Timestamp");
        
        log.info("签名头部 - X-Signature: {}, X-Timestamp: {}", 
            signature != null ? "已提供" : "缺失", 
            timestamp != null ? "已提供" : "缺失"
        );
        
        Object cachedRequest = request.getAttribute("cachedRequest");
        log.info("拦截器处理状态: {}", cachedRequest != null ? "已处理" : "未处理");
        
        return Map.of(
            "message", "测试接口响应",
            "uri", request.getRequestURI(),
            "hasSignature", signature != null,
            "hasTimestamp", timestamp != null,
            "interceptorProcessed", cachedRequest != null,
            "receivedData", data
        );
    }
} 