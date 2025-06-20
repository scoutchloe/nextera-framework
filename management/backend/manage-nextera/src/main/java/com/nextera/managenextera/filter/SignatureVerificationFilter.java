package com.nextera.managenextera.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextera.managenextera.interceptor.CachedBodyHttpServletRequest;
import com.nextera.managenextera.util.SignatureUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;

/**
 * 签名验证过滤器
 * 对新增角色接口进行签名验证，并处理请求体缓存
 */
@Slf4j
public class SignatureVerificationFilter implements Filter {

    private static final String SIGNATURE_HEADER = "X-Signature";
    private static final String TIMESTAMP_HEADER = "X-Timestamp";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String requestURI = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        log.info("SignatureVerificationFilter: ===== 过滤器被调用 =====");
        log.info("SignatureVerificationFilter: 请求URI: {}", requestURI);
        log.info("SignatureVerificationFilter: 请求方法: {}", method);

        // 检查是否是需要签名验证的接口
        if (isTargetRoleRequest(requestURI, method)) {
            log.info("SignatureVerificationFilter: ✅ 匹配到目标请求，开始签名验证");
            
            // 创建缓存请求对象
            CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(httpRequest);
            
            // 验证签名
            if (verifySignature(cachedRequest, httpResponse)) {
                log.info("SignatureVerificationFilter: 签名验证成功，继续处理请求");
                // 使用缓存的请求对象继续处理
                chain.doFilter(cachedRequest, response);
            }
            // 如果签名验证失败，已经在verifySignature方法中返回错误响应
        } else {
            log.info("SignatureVerificationFilter: ❌ 非目标请求，直接放行");
            chain.doFilter(request, response);
        }
    }

    /**
     * 判断是否是目标角色请求
     */
    private boolean isTargetRoleRequest(String requestURI, String method) {
        boolean isRolePath = "/api/system/role".equals(requestURI) || "/api/system/role/".equals(requestURI);
        boolean isPostMethod = "POST".equalsIgnoreCase(method);
        
        log.info("SignatureVerificationFilter: 路径匹配检查:");
        log.info("SignatureVerificationFilter: - 请求URI: '{}'", requestURI);
        log.info("SignatureVerificationFilter: - 路径匹配结果: {}", isRolePath);
        log.info("SignatureVerificationFilter: - 请求方法: '{}', POST匹配: {}", method, isPostMethod);
        log.info("SignatureVerificationFilter: - 最终匹配结果: {}", isRolePath && isPostMethod);
        
        return isRolePath && isPostMethod;
    }

    /**
     * 验证签名
     */
    private boolean verifySignature(CachedBodyHttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        try {
            // 获取签名和时间戳
            String signature = request.getHeader(SIGNATURE_HEADER);
            String timestampStr = request.getHeader(TIMESTAMP_HEADER);
            
            log.debug("SignatureVerificationFilter: 请求头检查 - X-Signature: {}, X-Timestamp: {}", 
                signature != null ? "已提供" : "缺失", timestampStr != null ? "已提供" : "缺失");
            
            if (!StringUtils.hasText(signature) || !StringUtils.hasText(timestampStr)) {
                log.warn("SignatureVerificationFilter: 签名验证失败：缺少必要的签名头部信息");
                writeErrorResponse(response, 400, "缺少签名或时间戳");
                return false;
            }
            
            long timestamp;
            try {
                timestamp = Long.parseLong(timestampStr);
            } catch (NumberFormatException e) {
                log.warn("SignatureVerificationFilter: 签名验证失败：时间戳格式错误 - {}", timestampStr);
                writeErrorResponse(response, 400, "时间戳格式错误");
                return false;
            }
            
            String body = request.getBody();
            
            if (!StringUtils.hasText(body)) {
                log.warn("SignatureVerificationFilter: 签名验证失败：请求体为空");
                writeErrorResponse(response, 400, "请求体为空");
                return false;
            }
            
            log.debug("SignatureVerificationFilter: 接收到的请求体: {}", body);
            log.debug("SignatureVerificationFilter: 接收到的签名: {}", signature);
            log.debug("SignatureVerificationFilter: 接收到的时间戳: {}", timestamp);
            
            // 解析请求体为Map
            @SuppressWarnings("unchecked")
            Map<String, Object> requestParams = objectMapper.readValue(body, Map.class);
            
            log.debug("SignatureVerificationFilter: 解析后的请求参数: {}", requestParams);
            
            // 处理permissionIds数组 - 确保与前端签名逻辑一致
            if (requestParams.containsKey("permissionIds") && requestParams.get("permissionIds") instanceof java.util.List) {
                @SuppressWarnings("unchecked")
                java.util.List<Integer> list = (java.util.List<Integer>) requestParams.get("permissionIds");
                if (!list.isEmpty()) {
                    String joinedIds = list.stream()
                            .sorted()
                            .map(String::valueOf)
                            .collect(java.util.stream.Collectors.joining(","));
                    requestParams.put("permissionIds", joinedIds);
                    log.debug("SignatureVerificationFilter: 处理permissionIds数组: {} -> {}", list, joinedIds);
                }
            }
            
            // 验证签名
            boolean isValid = SignatureUtil.verifySignature(requestParams, timestamp, signature);
            
            log.info("SignatureVerificationFilter: 签名验证结果: {}", isValid ? "成功" : "失败");
            
            if (!isValid) {
                log.warn("SignatureVerificationFilter: 签名验证失败：签名不匹配");
                writeErrorResponse(response, 400, "签名验证失败");
                return false;
            }
            
            // 标记签名验证已通过
            request.setAttribute("signatureVerified", true);
            
            return true;
            
        } catch (Exception e) {
            log.error("SignatureVerificationFilter: 签名验证过程中发生异常", e);
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
        
        log.debug("SignatureVerificationFilter: 返回错误响应: {}", errorJson);
    }
} 