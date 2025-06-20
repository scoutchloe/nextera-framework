package com.nextera.managenextera.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextera.managenextera.util.SignatureUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Map;

/**
 * 签名验证拦截器
 * 对新增角色接口进行签名验证
 */
@Slf4j
@Component
public class SignatureInterceptor implements HandlerInterceptor {

    private static final String SIGNATURE_HEADER = "X-Signature";
    private static final String TIMESTAMP_HEADER = "X-Timestamp";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String contentType = request.getContentType();
        
        log.info("SignatureInterceptor: ===== 拦截器被调用 =====");
        log.info("SignatureInterceptor: 请求URI: {}", requestURI);
        log.info("SignatureInterceptor: 请求方法: {}", method);
        log.info("SignatureInterceptor: Content-Type: {}", contentType);
        log.info("SignatureInterceptor: 请求头 X-Signature: {}", request.getHeader(SIGNATURE_HEADER) != null ? "存在" : "不存在");
        log.info("SignatureInterceptor: 请求头 X-Timestamp: {}", request.getHeader(TIMESTAMP_HEADER) != null ? "存在" : "不存在");
        
        // 检查是否是需要签名验证的接口
        if (isTargetRoleRequest(requestURI, method)) {
            log.info("SignatureInterceptor: ✅ 匹配到目标请求，开始签名验证");
            return handleSignatureVerification(request, response);
        }
        
        log.info("SignatureInterceptor: ❌ 非目标请求，直接放行: {} {}", method, requestURI);
        return true;
    }
    
    /**
     * 判断是否是目标角色请求
     */
    private boolean isTargetRoleRequest(String requestURI, String method) {
        // 精确匹配路径，修正为实际的控制器路径
        boolean isRolePath = "/api/system/role".equals(requestURI) || "/api/system/role/".equals(requestURI);
        boolean isPostMethod = "POST".equalsIgnoreCase(method);
        
        log.info("SignatureInterceptor: 路径匹配检查:");
        log.info("SignatureInterceptor: - 请求URI: '{}'", requestURI);
        log.info("SignatureInterceptor: - 目标路径1: '/system/role' 匹配: {}", "/api/system/role".equals(requestURI));
        log.info("SignatureInterceptor: - 目标路径2: '/system/role/' 匹配: {}", "/api/system/role/".equals(requestURI));
        log.info("SignatureInterceptor: - 路径匹配结果: {}", isRolePath);
        log.info("SignatureInterceptor: - 请求方法: '{}', POST匹配: {}", method, isPostMethod);
        log.info("SignatureInterceptor: - 最终匹配结果: {}", isRolePath && isPostMethod);
        
        return isRolePath && isPostMethod;
    }
    
    /**
     * 处理签名验证（新增角色接口）
     */
    private boolean handleSignatureVerification(HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("SignatureInterceptor: 开始验证角色新增接口签名: POST /system/role");
        
        try {
            // 获取签名和时间戳
            String signature = request.getHeader(SIGNATURE_HEADER);
            String timestampStr = request.getHeader(TIMESTAMP_HEADER);
            
            log.debug("SignatureInterceptor: 请求头检查 - X-Signature: {}, X-Timestamp: {}", 
                signature != null ? "已提供" : "缺失", timestampStr != null ? "已提供" : "缺失");
            
            if (!StringUtils.hasText(signature) || !StringUtils.hasText(timestampStr)) {
                log.warn("SignatureInterceptor: 签名验证失败：缺少必要的签名头部信息 - X-Signature: {}, X-Timestamp: {}", 
                    signature != null ? "存在" : "缺失", timestampStr != null ? "存在" : "缺失");
                writeErrorResponse(response, 400, "缺少签名或时间戳");
                return false;
            }
            
            long timestamp;
            try {
                timestamp = Long.parseLong(timestampStr);
            } catch (NumberFormatException e) {
                log.warn("SignatureInterceptor: 签名验证失败：时间戳格式错误 - {}", timestampStr);
                writeErrorResponse(response, 400, "时间戳格式错误");
                return false;
            }
            
            // 创建缓存请求对象以支持多次读取请求体
            CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
            String body = cachedRequest.getBody();
            
            if (!StringUtils.hasText(body)) {
                log.warn("SignatureInterceptor: 签名验证失败：请求体为空");
                writeErrorResponse(response, 400, "请求体为空");
                return false;
            }
            
            log.debug("SignatureInterceptor: 接收到的请求体: {}", body);
            log.debug("SignatureInterceptor: 接收到的签名: {}", signature);
            log.debug("SignatureInterceptor: 接收到的时间戳: {}", timestamp);
            
            // 解析请求体为Map
            @SuppressWarnings("unchecked")
            Map<String, Object> requestParams = objectMapper.readValue(body, Map.class);
            
            log.debug("SignatureInterceptor: 解析后的请求参数: {}", requestParams);
            
            // 处理permissionIds数组 - 确保与前端签名逻辑一致
            if (requestParams.containsKey("permissionIds") && requestParams.get("permissionIds") instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<Integer> list = (java.util.List<Integer>) requestParams.get("permissionIds");
                if (!list.isEmpty()) {
                    // 排序后转换为逗号分隔的字符串，与前端保持一致
                    String joinedIds = list.stream()
                            .sorted()
                            .map(String::valueOf)
                            .collect(java.util.stream.Collectors.joining(","));
                    requestParams.put("permissionIds", joinedIds);
                    log.debug("SignatureInterceptor: 处理permissionIds数组: {} -> {}", list, joinedIds);
                }
            }
            
            // 验证签名
            boolean isValid = SignatureUtil.verifySignature(requestParams, timestamp, signature);
            
            log.info("SignatureInterceptor: 签名验证结果: {}", isValid ? "成功" : "失败");
            
            if (!isValid) {
                log.warn("SignatureInterceptor: 签名验证失败：签名不匹配");
                log.debug("SignatureInterceptor: 验证参数: {}", requestParams);
                writeErrorResponse(response, 400, "签名验证失败");
                return false;
            }
            
            log.info("SignatureInterceptor: 签名验证成功，允许访问角色新增接口");
            
            // 将缓存的请求对象设置到request属性中，供控制器使用
            request.setAttribute("cachedRequest", cachedRequest);
            
            return true;
            
        } catch (Exception e) {
            log.error("SignatureInterceptor: 签名验证过程中发生异常", e);
            writeErrorResponse(response, 500, "签名验证异常: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 写入错误响应
     */
    private void writeErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        
        String errorJson = String.format(
            "{\"code\":%d,\"message\":\"%s\",\"data\":null,\"timestamp\":%d}", 
            status, message, System.currentTimeMillis()
        );
        response.getWriter().write(errorJson);
        response.getWriter().flush();
        
        log.debug("SignatureInterceptor: 返回错误响应: {}", errorJson);
    }
} 