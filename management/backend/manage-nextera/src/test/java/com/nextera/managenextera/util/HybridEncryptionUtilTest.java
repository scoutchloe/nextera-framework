package com.nextera.managenextera.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 混合加密工具类测试
 */
@DisplayName("混合加密工具类测试")
class HybridEncryptionUtilTest {

    private String rsaPublicKey;
    private String rsaPrivateKey;

    @BeforeEach
    void setUp() {
        // 初始化RSA密钥对
        RSAUtil.generateKeyPair();
        rsaPublicKey = RSAUtil.getPublicKey();
        rsaPrivateKey = RSAUtil.getPrivateKey();
    }

    @Test
    @DisplayName("测试混合加密解密基本功能")
    void testHybridEncryptDecrypt() {
        // 准备测试数据
        String originalData = "Hello, World!";
        
        // 混合加密
        String encryptedData = HybridEncryptionUtil.hybridEncrypt(originalData, rsaPublicKey);
        assertNotNull(encryptedData);
        assertNotEquals(originalData, encryptedData);
        
        // 混合解密
        String decryptedData = HybridEncryptionUtil.hybridDecrypt(encryptedData, rsaPrivateKey);
        assertEquals(originalData, decryptedData);
    }

    @Test
    @DisplayName("测试混合加密解密JSON数据")
    void testHybridEncryptDecryptJson() {
        // 准备测试数据
        String jsonData = "{\"id\":1,\"name\":\"测试角色\",\"permissions\":[1,2,3]}";
        
        // 混合加密
        String encryptedData = HybridEncryptionUtil.hybridEncrypt(jsonData, rsaPublicKey);
        assertNotNull(encryptedData);
        assertNotEquals(jsonData, encryptedData);
        
        // 验证加密数据是有效的JSON
        assertTrue(HybridEncryptionUtil.isValidHybridEncryptedData(encryptedData));
        
        // 混合解密
        String decryptedData = HybridEncryptionUtil.hybridDecrypt(encryptedData, rsaPrivateKey);
        assertEquals(jsonData, decryptedData);
    }

    @Test
    @DisplayName("测试混合加密解密中文数据")
    void testHybridEncryptDecryptChinese() {
        // 准备测试数据
        String chineseData = "这是一个包含中文的测试数据：系统管理员角色";
        
        // 混合加密
        String encryptedData = HybridEncryptionUtil.hybridEncrypt(chineseData, rsaPublicKey);
        assertNotNull(encryptedData);
        assertNotEquals(chineseData, encryptedData);
        
        // 混合解密
        String decryptedData = HybridEncryptionUtil.hybridDecrypt(encryptedData, rsaPrivateKey);
        assertEquals(chineseData, decryptedData);
    }

    @Test
    @DisplayName("测试混合加密解密大数据")
    void testHybridEncryptDecryptLargeData() {
        // 准备大量测试数据
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append("这是第").append(i).append("行测试数据，包含中英文混合内容 Test Data Line ").append(i).append("。");
        }
        String largeData = sb.toString();
        
        // 混合加密
        String encryptedData = HybridEncryptionUtil.hybridEncrypt(largeData, rsaPublicKey);
        assertNotNull(encryptedData);
        assertNotEquals(largeData, encryptedData);
        
        // 混合解密
        String decryptedData = HybridEncryptionUtil.hybridDecrypt(encryptedData, rsaPrivateKey);
        assertEquals(largeData, decryptedData);
    }

    @Test
    @DisplayName("测试每次加密结果不同（随机AES密钥）")
    void testEncryptionRandomness() {
        String originalData = "测试数据";
        
        // 多次加密同一数据
        String encrypted1 = HybridEncryptionUtil.hybridEncrypt(originalData, rsaPublicKey);
        String encrypted2 = HybridEncryptionUtil.hybridEncrypt(originalData, rsaPublicKey);
        String encrypted3 = HybridEncryptionUtil.hybridEncrypt(originalData, rsaPublicKey);
        
        // 每次加密结果都不同（因为使用随机AES密钥）
        assertNotEquals(encrypted1, encrypted2);
        assertNotEquals(encrypted2, encrypted3);
        assertNotEquals(encrypted1, encrypted3);
        
        // 但解密结果都相同
        assertEquals(originalData, HybridEncryptionUtil.hybridDecrypt(encrypted1, rsaPrivateKey));
        assertEquals(originalData, HybridEncryptionUtil.hybridDecrypt(encrypted2, rsaPrivateKey));
        assertEquals(originalData, HybridEncryptionUtil.hybridDecrypt(encrypted3, rsaPrivateKey));
    }

    @Test
    @DisplayName("测试无效加密数据验证")
    void testInvalidEncryptedData() {
        // 测试无效的JSON格式
        assertFalse(HybridEncryptionUtil.isValidHybridEncryptedData("invalid json"));
        assertFalse(HybridEncryptionUtil.isValidHybridEncryptedData("{}"));
        assertFalse(HybridEncryptionUtil.isValidHybridEncryptedData("{\"encryptedKey\":\"\"}"));
        assertFalse(HybridEncryptionUtil.isValidHybridEncryptedData("{\"encryptedData\":\"\"}"));
        
        // 测试有效的格式
        String validData = "{\"encryptedKey\":\"test\",\"encryptedData\":\"test\"}";
        assertTrue(HybridEncryptionUtil.isValidHybridEncryptedData(validData));
    }

    @Test
    @DisplayName("测试解密失败场景")
    void testDecryptionFailure() {
        // 测试用错误的私钥解密
        RSAUtil.generateKeyPair(); // 生成新的密钥对
        String wrongPrivateKey = RSAUtil.getPrivateKey();
        
        String originalData = "测试数据";
        String encryptedData = HybridEncryptionUtil.hybridEncrypt(originalData, rsaPublicKey);
        
        // 用错误的私钥解密应该失败
        assertThrows(RuntimeException.class, () -> {
            HybridEncryptionUtil.hybridDecrypt(encryptedData, wrongPrivateKey);
        });
    }

    @Test
    @DisplayName("测试空数据处理")
    void testEmptyData() {
        // 测试空字符串
        String emptyData = "";
        String encryptedData = HybridEncryptionUtil.hybridEncrypt(emptyData, rsaPublicKey);
        String decryptedData = HybridEncryptionUtil.hybridDecrypt(encryptedData, rsaPrivateKey);
        assertEquals(emptyData, decryptedData);
        
        // 测试null数据应该抛出异常
        assertThrows(RuntimeException.class, () -> {
            HybridEncryptionUtil.hybridEncrypt(null, rsaPublicKey);
        });
    }
} 