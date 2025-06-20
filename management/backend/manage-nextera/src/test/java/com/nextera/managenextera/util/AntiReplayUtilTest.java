package com.nextera.managenextera.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 防重放攻击工具类测试
 */
@ExtendWith(MockitoExtension.class)
class AntiReplayUtilTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AntiReplayUtil antiReplayUtil;

    @Test
    void testGenerateNonce() {
        String nonce = antiReplayUtil.generateNonce();
        assertNotNull(nonce);
        assertFalse(nonce.isEmpty());
        assertTrue(nonce.length() > 10);
        
        // 生成两个nonce应该不同
        String nonce2 = antiReplayUtil.generateNonce();
        assertNotEquals(nonce, nonce2);
    }

    @Test
    void testIsValidTimestamp() {
        long currentTime = System.currentTimeMillis();
        long windowMs = 5 * 60 * 1000; // 5分钟
        
        // 测试有效时间戳
        assertTrue(antiReplayUtil.isValidTimestamp(currentTime, windowMs));
        assertTrue(antiReplayUtil.isValidTimestamp(currentTime - 1000, windowMs)); // 1秒前
        assertTrue(antiReplayUtil.isValidTimestamp(currentTime + 1000, windowMs)); // 1秒后
        
        // 测试无效时间戳
        assertFalse(antiReplayUtil.isValidTimestamp(currentTime - windowMs - 1000, windowMs)); // 超出窗口
        assertFalse(antiReplayUtil.isValidTimestamp(currentTime + windowMs + 1000, windowMs)); // 超出窗口
    }

    @Test
    void testValidateAndRecordNonce_Success() {
        String nonce = "test-nonce-123";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), eq("used"), any(Duration.class)))
                .thenReturn(true);
        
        boolean result = antiReplayUtil.validateAndRecordNonce(nonce, 300);
        
        assertTrue(result);
        verify(valueOperations).setIfAbsent(eq("anti_replay:nonce:" + nonce), eq("used"), eq(Duration.ofSeconds(300)));
    }

    @Test
    void testValidateAndRecordNonce_AlreadyUsed() {
        String nonce = "test-nonce-123";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), eq("used"), any(Duration.class)))
                .thenReturn(false);
        
        boolean result = antiReplayUtil.validateAndRecordNonce(nonce, 300);
        
        assertFalse(result);
    }

    @Test
    void testValidateAndRecordNonce_NullNonce() {
        boolean result = antiReplayUtil.validateAndRecordNonce(null, 300);
        assertFalse(result);
        
        result = antiReplayUtil.validateAndRecordNonce("", 300);
        assertFalse(result);
        
        result = antiReplayUtil.validateAndRecordNonce("   ", 300);
        assertFalse(result);
    }

    @Test
    void testValidateUserSequence_FirstTime() {
        String userId = "user123";
        Long sequence = 1L;
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("anti_replay:sequence:" + userId)).thenReturn(null);
        
        boolean result = antiReplayUtil.validateUserSequence(userId, sequence);
        
        assertTrue(result);
        verify(valueOperations).set(eq("anti_replay:sequence:" + userId), eq("1"), eq(Duration.ofDays(30)));
    }

    @Test
    void testValidateUserSequence_ValidSequence() {
        String userId = "user123";
        Long sequence = 5L;
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("anti_replay:sequence:" + userId)).thenReturn("3");
        
        boolean result = antiReplayUtil.validateUserSequence(userId, sequence);
        
        assertTrue(result);
        verify(valueOperations).set(eq("anti_replay:sequence:" + userId), eq("5"), eq(Duration.ofDays(30)));
    }

    @Test
    void testValidateUserSequence_InvalidSequence() {
        String userId = "user123";
        Long sequence = 3L;
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("anti_replay:sequence:" + userId)).thenReturn("5");
        
        boolean result = antiReplayUtil.validateUserSequence(userId, sequence);
        
        assertFalse(result);
        verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void testGenerateSignature() {
        String method = "POST";
        String uri = "/api/test";
        String body = "{\"test\":\"data\"}";
        long timestamp = 1234567890L;
        String nonce = "test-nonce";
        String secretKey = "test-secret";
        
        String signature = antiReplayUtil.generateSignature(method, uri, body, timestamp, nonce, secretKey);
        
        assertNotNull(signature);
        assertEquals(64, signature.length()); // SHA-256 hex string length
        
        // 相同参数应该生成相同签名
        String signature2 = antiReplayUtil.generateSignature(method, uri, body, timestamp, nonce, secretKey);
        assertEquals(signature, signature2);
        
        // 不同参数应该生成不同签名
        String signature3 = antiReplayUtil.generateSignature(method, uri, body, timestamp, "different-nonce", secretKey);
        assertNotEquals(signature, signature3);
    }

    @Test
    void testValidateSignature() {
        String method = "POST";
        String uri = "/api/test";
        String body = "{\"test\":\"data\"}";
        long timestamp = 1234567890L;
        String nonce = "test-nonce";
        String secretKey = "test-secret";
        
        String signature = antiReplayUtil.generateSignature(method, uri, body, timestamp, nonce, secretKey);
        
        // 验证正确签名
        assertTrue(antiReplayUtil.validateSignature(method, uri, body, timestamp, nonce, signature, secretKey));
        
        // 验证错误签名
        assertFalse(antiReplayUtil.validateSignature(method, uri, body, timestamp, nonce, "wrong-signature", secretKey));
        
        // 验证null签名
        assertFalse(antiReplayUtil.validateSignature(method, uri, body, timestamp, nonce, null, secretKey));
    }

    @Test
    void testValidateRequest_Success() {
        // Mock Redis操作
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), eq("used"), any(Duration.class)))
                .thenReturn(true);
        when(valueOperations.get(anyString())).thenReturn(null);
        
        String userId = "user123";
        Long timestamp = System.currentTimeMillis();
        String nonce = "test-nonce";
        Long sequence = 1L;
        String method = "POST";
        String uri = "/api/test";
        String body = "{\"test\":\"data\"}";
        String secretKey = "test-secret";
        
        String signature = antiReplayUtil.generateSignature(method, uri, body, timestamp, nonce, secretKey);
        
        AntiReplayUtil.AntiReplayResult result = antiReplayUtil.validateRequest(
                userId, timestamp, nonce, sequence, signature,
                method, uri, body, secretKey,
                5 * 60 * 1000, true, true
        );
        
        assertTrue(result.isValid());
        assertEquals("验证通过", result.getMessage());
    }

    @Test
    void testValidateRequest_ExpiredTimestamp() {
        Long expiredTimestamp = System.currentTimeMillis() - 10 * 60 * 1000; // 10分钟前
        
        AntiReplayUtil.AntiReplayResult result = antiReplayUtil.validateRequest(
                "user123", expiredTimestamp, "nonce", 1L, "signature",
                "POST", "/api/test", "body", "secret",
                5 * 60 * 1000, false, false
        );
        
        assertFalse(result.isValid());
        assertEquals("请求已过期", result.getMessage());
    }

    @Test
    void testValidateRequest_InvalidNonce() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), eq("used"), any(Duration.class)))
                .thenReturn(false);
        
        AntiReplayUtil.AntiReplayResult result = antiReplayUtil.validateRequest(
                "user123", System.currentTimeMillis(), "used-nonce", null, null,
                "POST", "/api/test", "body", "secret",
                5 * 60 * 1000, false, false
        );
        
        assertFalse(result.isValid());
        assertEquals("无效的nonce或请求重复", result.getMessage());
    }
} 