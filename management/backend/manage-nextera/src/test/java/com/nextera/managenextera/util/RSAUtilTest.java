package com.nextera.managenextera.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RSA工具测试类
 */
public class RSAUtilTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testEncryptAndDecrypt() {
        String plainText = "Hello RSA Encryption!";
        
        // 加密
        String encrypted = RSAUtil.encrypt(plainText, RSAUtil.getPublicKey());
        assertNotNull(encrypted);
        assertFalse(encrypted.isEmpty());
        assertNotEquals(plainText, encrypted);
        
        // 解密
        String decrypted = RSAUtil.decrypt(encrypted);
        assertEquals(plainText, decrypted);
    }
    
    @Test
    public void testEncryptRoleData() throws Exception {
        // 创建测试角色数据
        Map<String, Object> roleData = new HashMap<>();
        roleData.put("id", 1L);
        roleData.put("roleCode", "ADMIN");
        roleData.put("roleName", "管理员");
        roleData.put("status", 1);
        roleData.put("description", "系统管理员角色");
        
        String jsonData = objectMapper.writeValueAsString(roleData);
        
        // 加密
        String encrypted = RSAUtil.encrypt(jsonData, RSAUtil.getPublicKey());
        assertNotNull(encrypted);
        assertFalse(encrypted.isEmpty());
        
        // 解密
        String decrypted = RSAUtil.decrypt(encrypted);
        assertEquals(jsonData, decrypted);
        
        // 验证解密后的数据格式
        @SuppressWarnings("unchecked")
        Map<String, Object> decryptedMap = objectMapper.readValue(decrypted, Map.class);
        assertEquals("ADMIN", decryptedMap.get("roleCode"));
        assertEquals("管理员", decryptedMap.get("roleName"));
        assertEquals(1, decryptedMap.get("status"));
    }
    
    @Test
    public void testGetPublicKey() {
        String publicKey = RSAUtil.getPublicKey();
        assertNotNull(publicKey);
        assertFalse(publicKey.isEmpty());
        assertTrue(publicKey.length() > 100); // RSA 2048位公钥应该很长
    }
    
    @Test
    public void testInvalidDecryption() {
        assertThrows(RuntimeException.class, () -> {
            RSAUtil.decrypt("invalid_encrypted_data");
        });
    }
} 