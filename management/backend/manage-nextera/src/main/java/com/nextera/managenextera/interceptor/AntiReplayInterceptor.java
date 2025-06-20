package com.nextera.managenextera.interceptor;

import com.nextera.managenextera.annotation.AntiReplay;
import com.nextera.managenextera.util.AntiReplayUtil;
import com.nextera.managenextera.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * 防重放攻击拦截器
 */
@Slf4j
@Component
public class AntiReplayInterceptor implements HandlerInterceptor {

    @Autowired
    private AntiReplayUtil antiReplayUtil;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Value("${anti-replay.secret-key:default-secret-key}")
    private String defaultSecretKey;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只处理HandlerMethod类型的handler
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        AntiReplay antiReplay = handlerMethod.getMethodAnnotation(AntiReplay.class);
        
        // 如果没有@AntiReplay注解，直接通过
        if (antiReplay == null) {
            return true;
        }
        
        try {
            // 获取请求头中的防重放参数
            String timestampStr = request.getHeader("X-Timestamp");
            String nonce = request.getHeader("X-Nonce");
            String sequenceStr = request.getHeader("X-Sequence");
            String signature = request.getHeader("X-Signature");
            
            // 获取用户ID（从JWT token中提取）
            String userId = getUserIdFromToken(request);
            
            // 解析参数
            Long timestamp = null;
            Long sequence = null;
            
            try {
                if (timestampStr != null) {
                    timestamp = Long.parseLong(timestampStr);
                }
                if (sequenceStr != null) {
                    sequence = Long.parseLong(sequenceStr);
                }
            } catch (NumberFormatException e) {
                log.warn("解析防重放参数失败: {}", e.getMessage());
                sendErrorResponse(response, "无效的请求参数");
                return false;
            }
            
            // 获取请求体
            String body = getRequestBody(request);
            
            // 获取密钥
            String secretKey = getSecretKey(antiReplay.secretKeyName());
            
            // 执行防重放验证
            AntiReplayUtil.AntiReplayResult result = antiReplayUtil.validateRequest(
                userId, timestamp, nonce, sequence, signature,
                request.getMethod(), request.getRequestURI(), body, secretKey,
                antiReplay.timeWindow(), antiReplay.enableSequence(), antiReplay.enableSignature()
            );
            
            if (!result.isValid()) {
                log.warn("防重放验证失败: {}, URI: {}, 用户: {}", result.getMessage(), request.getRequestURI(), userId);
                sendErrorResponse(response, result.getMessage());
                return false;
            }
            
            log.debug("防重放验证通过, URI: {}, 用户: {}", request.getRequestURI(), userId);
            return true;
            
        } catch (Exception e) {
            log.error("防重放验证异常", e);
            sendErrorResponse(response, "服务器内部错误");
            return false;
        }
    }
    
    /**
     * 从JWT token中获取用户ID
     */
    private String getUserIdFromToken(HttpServletRequest request) {
        try {
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                Long userId = jwtUtil.getUserIdFromToken(token);
                return userId != null ? userId.toString() : "anonymous";
            }
        } catch (Exception e) {
            log.debug("获取用户ID失败: {}", e.getMessage());
        }
        return "anonymous";
    }
    
    /**
     * 获取请求体内容
     */
    private String getRequestBody(HttpServletRequest request) {
        // 对于GET请求，返回空字符串
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            return "";
        }
        
        // 对于其他请求，这里简化处理，实际项目中可能需要缓存请求体
        return request.getQueryString() != null ? request.getQueryString() : "";
    }
    
    /**
     * 获取密钥
     */
    private String getSecretKey(String secretKeyName) {
        // 这里可以从配置文件或其他地方获取密钥
        // 简化处理，直接返回默认密钥
        return defaultSecretKey;
    }
    
    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":400,\"message\":\"" + message + "\",\"data\":null}");
    }
} 