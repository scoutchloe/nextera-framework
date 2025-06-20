package com.nextera.managenextera.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AES工具类测试
 */
@DisplayName("AES工具类测试")
class AESUtilTest {

    @Test
    @DisplayName("测试AES密钥生成")
    void testGenerateKey() {
        String key = AESUtil.generateKey();
        assertNotNull(key);
        assertTrue(key.length() > 0);
        assertTrue(AESUtil.isValidKey(key));
        
        // 每次生成的密钥都应该不同
        String key2 = AESUtil.generateKey();
        assertNotEquals(key, key2);
    }

    @Test
    @DisplayName("测试AES加密解密基本功能")
    void testEncryptDecrypt() {
        String key = AESUtil.generateKey();
        String plainText = "Hello, World!";
        
        // 加密
        String encrypted = AESUtil.encrypt(plainText, key);
        assertNotNull(encrypted);
        assertNotEquals(plainText, encrypted);
        
        // 解密
        String decrypted = AESUtil.decrypt(encrypted, key);
        assertEquals(plainText, decrypted);
    }

    @Test
    @DisplayName("测试AES加密解密中文数据")
    void testEncryptDecryptChinese() {
        String key = AESUtil.generateKey();
        String chineseText = "这是一个包含中文的测试数据：系统管理员角色";
        
        // 加密
        String encrypted = AESUtil.encrypt(chineseText, key);
        assertNotNull(encrypted);
        assertNotEquals(chineseText, encrypted);
        
        // 解密
        String decrypted = AESUtil.decrypt(encrypted, key);
        assertEquals(chineseText, decrypted);
    }

    @Test
    @DisplayName("测试AES加密解密JSON数据")
    void testEncryptDecryptJson() {
        String key = AESUtil.generateKey();
        String jsonData = "{\"id\":1,\"name\":\"测试角色\",\"permissions\":[1,2,3],\"description\":\"这是一个测试角色\"}";
        
        // 加密
        String encrypted = AESUtil.encrypt(jsonData, key);
        assertNotNull(encrypted);
        assertNotEquals(jsonData, encrypted);
        
        // 解密
        String decrypted = AESUtil.decrypt(encrypted, key);
        assertEquals(jsonData, decrypted);
    }

    @Test
    @DisplayName("测试AES加密解密大数据")
    void testEncryptDecryptLargeData() {
        String key = AESUtil.generateKey();
        
        // 生成大量数据
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("这是第").append(i).append("行测试数据，包含中英文混合内容 Test Data Line ").append(i).append("。");
        }
        String largeData = sb.toString();
        
        // 加密
        String encrypted = AESUtil.encrypt(largeData, key);
        assertNotNull(encrypted);
        assertNotEquals(largeData, encrypted);
        
        // 解密
        String decrypted = AESUtil.decrypt(encrypted, key);
        assertEquals(largeData, decrypted);
    }

    @Test
    @DisplayName("测试每次加密结果不同（随机IV）")
    void testEncryptionRandomness() {
        String key = AESUtil.generateKey();
        String plainText = "测试数据";
        
        // 多次加密同一数据
        String encrypted1 = AESUtil.encrypt(plainText, key);
        String encrypted2 = AESUtil.encrypt(plainText, key);
        String encrypted3 = AESUtil.encrypt(plainText, key);
        
        // 每次加密结果都不同（因为使用随机IV）
        assertNotEquals(encrypted1, encrypted2);
        assertNotEquals(encrypted2, encrypted3);
        assertNotEquals(encrypted1, encrypted3);
        
        // 但解密结果都相同
        assertEquals(plainText, AESUtil.decrypt(encrypted1, key));
        assertEquals(plainText, AESUtil.decrypt(encrypted2, key));
        assertEquals(plainText, AESUtil.decrypt(encrypted3, key));
    }

    @Test
    @DisplayName("测试密钥验证")
    void testKeyValidation() {
        // 有效密钥
        String validKey = AESUtil.generateKey();
        assertTrue(AESUtil.isValidKey(validKey));
        
        // 无效密钥
        assertFalse(AESUtil.isValidKey("invalid_key"));
        assertFalse(AESUtil.isValidKey(""));
        assertFalse(AESUtil.isValidKey(null));
        assertFalse(AESUtil.isValidKey("dGVzdA==")); // 太短的Base64
    }

    @Test
    @DisplayName("测试空数据处理")
    void testEmptyData() {
        String key = AESUtil.generateKey();
        
        // 测试空字符串
        String emptyData = "";
        String encrypted = AESUtil.encrypt(emptyData, key);
        String decrypted = AESUtil.decrypt(encrypted, key);
        assertEquals(emptyData, decrypted);
    }

    @Test
    @DisplayName("测试加密失败场景")
    void testEncryptionFailure() {
        String plainText = "测试数据";
        
        // 使用无效密钥加密应该失败
        assertThrows(RuntimeException.class, () -> {
            AESUtil.encrypt(plainText, "invalid_key");
        });
        
        // 使用null密钥加密应该失败
        assertThrows(RuntimeException.class, () -> {
            AESUtil.encrypt(plainText, null);
        });
    }

    @Test
    @DisplayName("测试解密失败场景")
    void testDecryptionFailure() {
        String key = AESUtil.generateKey();
        String plainText = "测试数据";
        String encrypted = AESUtil.encrypt(plainText, key);
        
        // 使用错误密钥解密应该失败
        String wrongKey = AESUtil.generateKey();
        assertThrows(RuntimeException.class, () -> {
            AESUtil.decrypt(encrypted, wrongKey);
        });
        
        // 使用无效密钥解密应该失败
        assertThrows(RuntimeException.class, () -> {
            AESUtil.decrypt(encrypted, "invalid_key");
        });
        
        // 解密无效数据应该失败
        assertThrows(RuntimeException.class, () -> {
            AESUtil.decrypt("invalid_encrypted_data", key);
        });
    }

    @Test
    @DisplayName("测试null数据处理")
    void testNullData() {
        String key = AESUtil.generateKey();
        
        // 加密null数据应该失败
        assertThrows(RuntimeException.class, () -> {
            AESUtil.encrypt(null, key);
        });
        
        // 解密null数据应该失败
        assertThrows(RuntimeException.class, () -> {
            AESUtil.decrypt(null, key);
        });
    }
} 