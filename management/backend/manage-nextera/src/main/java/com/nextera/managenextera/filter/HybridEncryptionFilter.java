package com.nextera.managenextera.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextera.managenextera.interceptor.CachedBodyHttpServletRequest;
import com.nextera.managenextera.util.HybridEncryptionUtil;
import com.nextera.managenextera.util.RSAUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 混合加密解密过滤器
 * 专门处理角色更新接口的RSA+AES解密
 */
@Slf4j
public class HybridEncryptionFilter implements Filter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            // 检查是否是需要解密的角色更新请求
            if (isTargetRoleUpdateRequest(httpRequest.getRequestURI(), httpRequest.getMethod())) {
                log.info("HybridEncryptionFilter: 检测到角色更新请求，开始处理混合加密解密");
                
                // 检查是否有加密标识头部
                String encryptedHeader = httpRequest.getHeader("X-Encrypted");
                String encryptionTypeHeader = httpRequest.getHeader("X-Encryption-Type");
                if (!"true".equals(encryptedHeader) || !"hybrid".equals(encryptionTypeHeader)) {
                    log.warn("HybridEncryptionFilter: 未检测到混合加密标识，跳过解密处理");
                    log.debug("HybridEncryptionFilter: X-Encrypted={}, X-Encryption-Type={}", encryptedHeader, encryptionTypeHeader);
                    chain.doFilter(request, response);
                    return;
                }

                // 缓存请求体以便多次读取
                CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(httpRequest);
                
                // 读取加密的请求体
                String encryptedBody = cachedRequest.getBody();
                log.debug("HybridEncryptionFilter: 接收到加密数据: {}", encryptedBody);

                if (encryptedBody == null || encryptedBody.trim().isEmpty()) {
                    log.error("HybridEncryptionFilter: 请求体为空");
                    sendErrorResponse(httpResponse, "请求体不能为空");
                    return;
                }

                try {
                    // 解密数据
                    String decryptedData = HybridEncryptionUtil.hybridDecrypt(encryptedBody, RSAUtil.getPrivateKey());
                    log.info("HybridEncryptionFilter: 混合解密成功");
                    log.debug("HybridEncryptionFilter: 解密后数据: {}", decryptedData);

                    // 创建包含解密数据的新请求
                    DecryptedBodyHttpServletRequest decryptedRequest = new DecryptedBodyHttpServletRequest(
                            cachedRequest, decryptedData);

                    // 设置解密成功标识
                    decryptedRequest.setAttribute("hybridDecrypted", true);
                    decryptedRequest.setAttribute("originalEncryptedBody", encryptedBody);

                    log.info("HybridEncryptionFilter: 混合解密处理完成，继续处理请求");
                    chain.doFilter(decryptedRequest, response);
                    return;

                } catch (Exception e) {
                    log.error("HybridEncryptionFilter: 混合解密失败", e);
                    sendErrorResponse(httpResponse, "数据解密失败: " + e.getMessage());
                    return;
                }
            }

            // 非目标请求，直接放行
            chain.doFilter(request, response);

        } catch (Exception e) {
            log.error("HybridEncryptionFilter: 过滤器处理异常", e);
            sendErrorResponse(httpResponse, "请求处理异常: " + e.getMessage());
        }
    }

    /**
     * 检查是否是目标角色更新请求
     */
    private boolean isTargetRoleUpdateRequest(String requestURI, String method) {
        // 支持多种路径格式
        boolean isRolePath = "/system/role".equals(requestURI) || 
                            "/system/role/".equals(requestURI) ||
                            "/api/system/role".equals(requestURI) || 
                            "/api/system/role/".equals(requestURI);
        boolean isPutMethod = "PUT".equalsIgnoreCase(method);
        
        boolean isTarget = isRolePath && isPutMethod;
        log.debug("HybridEncryptionFilter: 路径匹配检查 - URI: {}, 方法: {}, 是否目标请求: {}", 
                 requestURI, method, isTarget);
        return isTarget;
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json;charset=UTF-8");
        
        String errorResponse = String.format(
            "{\"code\": 400, \"message\": \"%s\", \"data\": null}", 
            message.replace("\"", "\\\"")
        );
        
        response.getWriter().write(errorResponse);
        response.getWriter().flush();
    }

    /**
     * 包含解密数据的HttpServletRequest包装器
     */
    private static class DecryptedBodyHttpServletRequest extends CachedBodyHttpServletRequest {
        private final String decryptedBody;

        public DecryptedBodyHttpServletRequest(HttpServletRequest request, String decryptedBody) {
            super(request, decryptedBody); // 使用父类的构造函数直接传入解密后的内容
            this.decryptedBody = decryptedBody;
        }

        public String getBody() {
            return decryptedBody;
        }

        public byte[] getBodyBytes() {
            return decryptedBody.getBytes(StandardCharsets.UTF_8);
        }
    }
} 