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
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;

/**
 * 混合加密解密过滤器
 * 用于处理编辑角色接口的混合加密解密（RSA + AES）
 */
@Slf4j
@Component
@Order(1)
public class RSADecryptionFilter implements Filter {

    private static final String ENCRYPTED_HEADER = "X-Encrypted";
    private static final String ENCRYPTION_TYPE_HEADER = "X-Encryption-Type";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestURI = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        
        log.debug("RSA过滤器检查接口: {} {}", method, requestURI);
        
        // 检查是否是需要RSA解密的接口（支持带/api前缀的路径）
        if ((requestURI.equals("/system/role") || requestURI.equals("/api/system/role")) && "PUT".equals(method)) {
            // 检查是否是加密请求
            String encrypted = httpRequest.getHeader(ENCRYPTED_HEADER);
            if ("true".equals(encrypted)) {
                String encryptionType = httpRequest.getHeader(ENCRYPTION_TYPE_HEADER);
                
                // 如果是混合加密类型，让专门的混合加密过滤器处理
                if ("hybrid".equals(encryptionType)) {
                    log.debug("检测到混合加密请求，交由HybridEncryptionFilter处理");
                    chain.doFilter(request, response);
                    return;
                }
                
                log.info("检测到加密请求，加密类型: {}，开始解密处理", encryptionType);
                
                try {
                    // 处理解密
                    HttpServletRequest decryptedRequest = handleDecryption(httpRequest, encryptionType);
                    if (decryptedRequest != null) {
                        // 使用解密后的请求继续处理
                        chain.doFilter(decryptedRequest, response);
                        return;
                    } else {
                        // 解密失败，返回错误
                        writeErrorResponse(httpResponse, 400, "数据解密失败");
                        return;
                    }
                } catch (Exception e) {
                    log.error("解密过程中发生异常", e);
                    writeErrorResponse(httpResponse, 500, "解密异常");
                    return;
                }
            }
        }
        
        // 非加密请求或其他接口，直接继续处理
        chain.doFilter(request, response);
    }
    
    /**
     * 处理解密（支持RSA和混合加密）
     */
    private HttpServletRequest handleDecryption(HttpServletRequest request, String encryptionType) throws Exception {
        // 读取请求体（加密的数据）
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
        String encryptedBody = cachedRequest.getBody();
        
        if (!StringUtils.hasText(encryptedBody)) {
            log.warn("加密请求体为空");
            return null;
        }
        
        log.debug("收到加密数据: {}", encryptedBody);
        
        // 根据加密类型选择解密方式
        String decryptedBody;
        try {
            if ("hybrid".equals(encryptionType)) {
                // 混合加密解密
                log.debug("使用混合加密解密");
                decryptedBody = HybridEncryptionUtil.hybridDecrypt(encryptedBody, RSAUtil.getPrivateKey());
            } else {
                // 传统RSA解密（向后兼容）
                log.debug("使用传统RSA解密");
                decryptedBody = RSAUtil.decrypt(encryptedBody);
            }
            log.debug("解密成功，明文数据: {}", decryptedBody);
        } catch (Exception e) {
            log.error("解密失败，加密类型: {}", encryptionType, e);
            return null;
        }
        
        // 验证解密后的数据格式
        try {
            objectMapper.readValue(decryptedBody, Map.class);
        } catch (Exception e) {
            log.error("解密后数据格式错误", e);
            return null;
        }
        
        // 创建包含解密数据的新请求包装器
        CachedBodyHttpServletRequest decryptedRequest = new CachedBodyHttpServletRequest(request, decryptedBody);
        
        // 在请求中添加解密标记和解密后的数据
        decryptedRequest.setAttribute("DECRYPTED", true);
        decryptedRequest.setAttribute("DECRYPTED_DATA", decryptedBody);
        decryptedRequest.setAttribute("ENCRYPTION_TYPE", encryptionType != null ? encryptionType : "rsa");
        
        log.info("解密成功，加密类型: {}，解密后数据长度: {}", encryptionType, decryptedBody.length());
        return decryptedRequest;
    }
    
    /**
     * 写入错误响应
     */
    private void writeErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        
        String errorJson = String.format("{\"code\":%d,\"message\":\"%s\"}", status, message);
        response.getWriter().write(errorJson);
        response.getWriter().flush();
    }
} 