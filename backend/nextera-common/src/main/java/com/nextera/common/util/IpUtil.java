package com.nextera.common.util;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author Scout
 * @date 2025-06-16 10:24
 * @since 1.0
 */
public class IpUtil {
    /**
     * 获取客户端IP地址
     */
    public static String getClientIpAddress() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return "127.0.0.1";
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理多个IP的情况，取第一个
        if (StrUtil.isNotBlank(ip) && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return StrUtil.isBlank(ip) ? "127.0.0.1" : ip;
    }


    /**
     * 获取用户代理
     */
    private static String getUserAgent() {
        HttpServletRequest request = getCurrentRequest();
        if (request != null) {
            return request.getHeader("User-Agent");
        }
        return "Unknown";
    }

    /**
     * 获取当前请求
     */
    private static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}