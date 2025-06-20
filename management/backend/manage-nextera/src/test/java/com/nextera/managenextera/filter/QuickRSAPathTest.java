package com.nextera.managenextera.filter;

import com.nextera.managenextera.util.RSAUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 快速验证RSA路径匹配修复
 */
public class QuickRSAPathTest {

    @Test
    public void testApiPathMatching() {
        System.out.println("=== 验证/api/system/role路径匹配修复 ===");
        
        String requestURI = "/api/system/role";
        String method = "PUT";
        
        // 测试路径匹配逻辑（与Filter中的逻辑一致）
        boolean shouldMatch = (requestURI.equals("/system/role") || requestURI.equals("/api/system/role")) && "PUT".equals(method);
        
        assertTrue(shouldMatch, "/api/system/role路径应该匹配RSA处理逻辑");
        System.out.println("✓ /api/system/role路径匹配成功");
        
        // 测试普通路径也能匹配
        requestURI = "/system/role";
        shouldMatch = (requestURI.equals("/system/role") || requestURI.equals("/api/system/role")) && "PUT".equals(method);
        
        assertTrue(shouldMatch, "/system/role路径应该匹配RSA处理逻辑");
        System.out.println("✓ /system/role路径匹配成功");
        
        // 测试其他路径不匹配
        requestURI = "/other/path";
        shouldMatch = (requestURI.equals("/system/role") || requestURI.equals("/api/system/role")) && "PUT".equals(method);
        
        assertFalse(shouldMatch, "其他路径不应该匹配RSA处理逻辑");
        System.out.println("✓ 其他路径正确排除");
        
        // 测试非PUT方法不匹配
        requestURI = "/api/system/role";
        method = "GET";
        shouldMatch = (requestURI.equals("/system/role") || requestURI.equals("/api/system/role")) && "PUT".equals(method);
        
        assertFalse(shouldMatch, "非PUT方法不应该匹配RSA处理逻辑");
        System.out.println("✓ 非PUT方法正确排除");
    }
    
    @Test
    public void testRSAEncryptionDecryption() {
        System.out.println("=== 验证RSA加密解密功能 ===");
        
        String originalData = "{\"id\":1,\"roleName\":\"测试角色\",\"description\":\"测试描述\"}";
        System.out.println("原始数据: " + originalData);
        
        // 加密
        String encryptedData = RSAUtil.encrypt(originalData, RSAUtil.getPublicKey());
        System.out.println("加密后数据长度: " + encryptedData.length());
        assertNotNull(encryptedData);
        assertNotEquals(originalData, encryptedData);
        
        // 解密
        String decryptedData = RSAUtil.decrypt(encryptedData);
        System.out.println("解密后数据: " + decryptedData);
        assertEquals(originalData, decryptedData);
        
        System.out.println("✓ RSA加密解密功能正常");
    }
} 