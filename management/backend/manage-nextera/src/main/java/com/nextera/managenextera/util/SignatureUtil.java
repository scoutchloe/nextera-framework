package com.nextera.managenextera.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * 签名工具类
 * 用于API接口的签名生成和验证
 */
@Slf4j
public class SignatureUtil {
    
    /**
     * 签名密钥（实际项目中应该从配置文件或环境变量获取）
     */
    private static final String SECRET_KEY = "nextera_role_management_secret_key_2025";
    
    /**
     * 签名有效期（毫秒），5分钟
     */
    private static final long SIGNATURE_VALIDITY_PERIOD = 5 * 60 * 1000;
    
    /**
     * 生成签名
     * @param params 参数Map
     * @param timestamp 时间戳
     * @return 签名字符串
     */
    public static String generateSignature(Map<String, Object> params, long timestamp) {
        try {
            // 创建TreeMap自动排序
            TreeMap<String, Object> sortedParams = new TreeMap<>(params);
            
            // 添加时间戳
            sortedParams.put("timestamp", timestamp);
            
            // 构建签名字符串
            StringBuilder signBuilder = new StringBuilder();
            for (Map.Entry<String, Object> entry : sortedParams.entrySet()) {
                if (entry.getValue() != null) {
                    signBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
                }
            }
            
            // 添加密钥
            signBuilder.append("key=").append(SECRET_KEY);
            
            String signString = signBuilder.toString();
            
            log.debug("生成签名字符串: {}", signString);
            
            // MD5加密
            return DigestUtils.md5DigestAsHex(signString.getBytes(StandardCharsets.UTF_8)).toUpperCase();
            
        } catch (Exception e) {
            log.error("生成签名失败", e);
            throw new RuntimeException("生成签名失败", e);
        }
    }
    
    /**
     * 验证签名
     * @param params 参数Map
     * @param timestamp 时间戳
     * @param signature 签名
     * @return 验证结果
     */
    public static boolean verifySignature(Map<String, Object> params, long timestamp, String signature) {
        try {
            // 检查时间戳是否在有效期内
            long currentTime = System.currentTimeMillis();
            if (Math.abs(currentTime - timestamp) > SIGNATURE_VALIDITY_PERIOD) {
                log.warn("签名时间戳超出有效期: {}, 当前时间: {}", timestamp, currentTime);
                return false;
            }
            
            // 生成期望的签名
            String expectedSignature = generateSignature(params, timestamp);
            
            // 比较签名
            boolean isValid = expectedSignature.equals(signature);
            
            if (!isValid) {
                log.warn("签名验证失败, 期望: {}, 实际: {}", expectedSignature, signature);
            }
            
            return isValid;
            
        } catch (Exception e) {
            log.error("验证签名失败", e);
            return false;
        }
    }
    
    /**
     * 从对象中提取签名参数
     * @param obj 对象
     * @return 参数Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> extractSignatureParams(Object obj) {
        Map<String, Object> params = new TreeMap<>();
        
        if (obj == null) {
            return params;
        }
        
        // 使用反射提取对象属性
        Arrays.stream(obj.getClass().getSuperclass().getDeclaredFields())
                .forEach(field -> {
                    try {
                        field.setAccessible(true);
                        Object value = field.get(obj);
                        if (value != null && !field.getName().equals("sign") && !field.getName().equals("timestamp")) {
                            // 特殊处理List类型的permissionIds
                            if (field.getName().equals("permissionIds") && value instanceof java.util.List) {
                                java.util.List<Long> list = (java.util.List<Long>) value;
                                if (!list.isEmpty()) {
                                    // 排序后转换为逗号分隔的字符串
                                    String joinedIds = list.stream()
                                            .sorted()
                                            .map(String::valueOf)
                                            .collect(java.util.stream.Collectors.joining(","));
                                    params.put(field.getName(), joinedIds);
                                }
                            } else {
                                params.put(field.getName(), value);
                            }
                        }
                    } catch (Exception e) {
                        log.warn("提取字段值失败: {}", field.getName(), e);
                    }
                });
        
        return params;
    }
} 