package com.nextera.gateway.utils;

/**
 * @author Scout
 * @date 2025-06-16 17:40
 * @since 1.0
 */
public final class Knife4jUtil {

    public static boolean isKnife4jResource(String path) {
        return path != null && (
                path.contains("/doc.html") ||
                        path.contains("/v3/api-docs") ||
                        path.contains("/webjars/") ||
                        path.contains("/swagger-resources") ||
                        path.contains("/swagger-ui") ||
                        path.equals("/favicon.ico")
        );
    }
}