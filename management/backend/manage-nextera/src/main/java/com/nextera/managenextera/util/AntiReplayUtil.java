package com.nextera.managenextera.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.UUID;

/**
 * 防重放攻击工具类
 */
@Component
public class AntiReplayUtil {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    private static final String NONCE_PREFIX = "anti_replay:nonce:";
    private static final String SEQUENCE_PREFIX = "anti_replay:sequence:";
    
    /**
     * 生成nonce
     */
    public String generateNonce() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 验证时间戳是否在有效期内
     * @param timestamp 时间戳
     * @param windowMs 时间窗口（毫秒）
     * @return 是否有效
     */
    public boolean isValidTimestamp(long timestamp, long windowMs) {
        long currentTime = System.currentTimeMillis();
        return Math.abs(currentTime - timestamp) <= windowMs;
    }
    
    /**
     * 验证并记录nonce
     * @param nonce 随机数
     * @param expireSeconds 过期时间（秒）
     * @return 是否有效（未被使用过）
     */
    public boolean validateAndRecordNonce(String nonce, long expireSeconds) {
        if (nonce == null || nonce.trim().isEmpty()) {
            return false;
        }
        
        String key = NONCE_PREFIX + nonce;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, "used", Duration.ofSeconds(expireSeconds));
        return success != null && success;
    }
    
    /**
     * 验证用户序列号
     * @param userId 用户ID
     * @param sequence 序列号
     * @return 是否有效（大于上次序列号）
     */
    public boolean validateUserSequence(String userId, Long sequence) {
        if (userId == null || sequence == null) {
            return false;
        }
        
        String key = SEQUENCE_PREFIX + userId;
        String lastSequenceStr = redisTemplate.opsForValue().get(key);
        
        long lastSequence = 0;
        if (lastSequenceStr != null) {
            try {
                lastSequence = Long.parseLong(lastSequenceStr);
            } catch (NumberFormatException e) {
                // 如果解析失败，认为是第一次请求
                lastSequence = 0;
            }
        }
        
        if (sequence > lastSequence) {
            // 更新序列号
            redisTemplate.opsForValue().set(key, sequence.toString(), Duration.ofDays(30));
            return true;
        }
        
        return false;
    }
    
    /**
     * 生成请求签名
     * @param method HTTP方法
     * @param uri 请求URI
     * @param body 请求体
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param secretKey 密钥
     * @return 签名
     */
    public String generateSignature(String method, String uri, String body, 
                                  long timestamp, String nonce, String secretKey) {
        try {
            String data = method + "|" + uri + "|" + body + "|" + timestamp + "|" + nonce + "|" + secretKey;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    /**
     * 验证请求签名
     * @param method HTTP方法
     * @param uri 请求URI
     * @param body 请求体
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param signature 签名
     * @param secretKey 密钥
     * @return 是否有效
     */
    public boolean validateSignature(String method, String uri, String body, 
                                   long timestamp, String nonce, String signature, String secretKey) {
        if (signature == null) {
            return false;
        }
        
        String expectedSignature = generateSignature(method, uri, body, timestamp, nonce, secretKey);
        return signature.equals(expectedSignature);
    }
    
    /**
     * 综合验证防重放攻击
     * @param userId 用户ID
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param sequence 序列号（可选）
     * @param signature 签名（可选）
     * @param method HTTP方法
     * @param uri 请求URI
     * @param body 请求体
     * @param secretKey 密钥
     * @param timeWindowMs 时间窗口（毫秒）
     * @param enableSequence 是否启用序列号验证
     * @param enableSignature 是否启用签名验证
     * @return 验证结果
     */
    public AntiReplayResult validateRequest(String userId, Long timestamp, String nonce, 
                                          Long sequence, String signature,
                                          String method, String uri, String body, String secretKey,
                                          long timeWindowMs, boolean enableSequence, boolean enableSignature) {
        
        // 1. 验证时间戳
        if (timestamp == null || !isValidTimestamp(timestamp, timeWindowMs)) {
            return new AntiReplayResult(false, "请求已过期");
        }
        
        // 2. 验证nonce
        if (nonce == null || !validateAndRecordNonce(nonce, timeWindowMs / 1000 + 60)) {
            return new AntiReplayResult(false, "无效的nonce或请求重复");
        }
        
        // 3. 验证序列号（如果启用）
        if (enableSequence && (sequence == null || !validateUserSequence(userId, sequence))) {
            return new AntiReplayResult(false, "无效的序列号");
        }
        
        // 4. 验证签名（如果启用）
        if (enableSignature && !validateSignature(method, uri, body, timestamp, nonce, signature, secretKey)) {
            return new AntiReplayResult(false, "签名验证失败");
        }
        
        return new AntiReplayResult(true, "验证通过");
    }
    
    /**
     * 防重放验证结果
     */
    public static class AntiReplayResult {
        private final boolean valid;
        private final String message;
        
        public AntiReplayResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
    }
} 