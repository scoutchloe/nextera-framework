package com.nextera.managenextera.util;

import com.nextera.managenextera.dto.SysRoleSignDTO;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 签名工具测试类
 */
public class SignatureUtilTest {

    @Test
    public void testGenerateAndVerifySignature() {
        // 创建测试角色数据
        SysRoleSignDTO roleDTO = new SysRoleSignDTO();
        roleDTO.setRoleCode("TEST_ROLE");
        roleDTO.setRoleName("测试角色");
        roleDTO.setStatus(1);
        roleDTO.setDescription("这是一个测试角色");
        roleDTO.setPermissionIds(Arrays.asList(1L, 3L, 2L)); // 故意打乱顺序
        
        long timestamp = System.currentTimeMillis();
        
        // 提取签名参数
        Map<String, Object> params = SignatureUtil.extractSignatureParams(roleDTO);
        
        // 生成签名
        String signature = SignatureUtil.generateSignature(params, timestamp);
        
        assertNotNull(signature);
        assertFalse(signature.isEmpty());
        
        // 验证签名
        boolean isValid = SignatureUtil.verifySignature(params, timestamp, signature);
        assertTrue(isValid);
        
        // 测试错误签名
        boolean isInvalid = SignatureUtil.verifySignature(params, timestamp, "WRONG_SIGNATURE");
        assertFalse(isInvalid);
        
        // 测试过期时间戳
        long expiredTimestamp = timestamp - 10 * 60 * 1000; // 10分钟前
        boolean isExpired = SignatureUtil.verifySignature(params, expiredTimestamp, signature);
        assertFalse(isExpired);
    }
    
    @Test
    public void testExtractSignatureParams() {
        SysRoleSignDTO roleDTO = new SysRoleSignDTO();
        roleDTO.setRoleCode("TEST_ROLE");
        roleDTO.setRoleName("测试角色");
        roleDTO.setStatus(1);
        roleDTO.setDescription("这是一个测试角色");
        roleDTO.setPermissionIds(Arrays.asList(3L, 1L, 2L)); // 故意打乱顺序
        roleDTO.setSign("dummy_sign");
        roleDTO.setTimestamp(System.currentTimeMillis());
        
        Map<String, Object> params = SignatureUtil.extractSignatureParams(roleDTO);
        
        assertEquals("TEST_ROLE", params.get("roleCode"));
        assertEquals("测试角色", params.get("roleName"));
        assertEquals(1, params.get("status"));
        assertEquals("这是一个测试角色", params.get("description"));
        assertEquals("1,2,3", params.get("permissionIds")); // 应该是排序后的字符串
        
        // 确保不包含sign和timestamp
        assertFalse(params.containsKey("sign"));
        assertFalse(params.containsKey("timestamp"));
    }
    
    @Test
    public void testHeaderBasedSignatureVerification() {
        // 测试基于header的签名验证（模拟拦截器中的处理）
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("roleCode", "ADMIN");
        requestParams.put("roleName", "管理员");
        requestParams.put("status", 1);
        requestParams.put("description", "系统管理员");
        requestParams.put("permissionIds", "1,2,3"); // 已排序的字符串
        
        long timestamp = System.currentTimeMillis();
        
        // 生成签名
        String signature = SignatureUtil.generateSignature(requestParams, timestamp);
        
        assertNotNull(signature);
        assertFalse(signature.isEmpty());
        
        // 验证签名
        boolean isValid = SignatureUtil.verifySignature(requestParams, timestamp, signature);
        assertTrue(isValid);
        
        // 测试错误签名
        boolean isInvalid = SignatureUtil.verifySignature(requestParams, timestamp, "WRONG_SIGNATURE");
        assertFalse(isInvalid);
        
        // 测试参数篡改
        requestParams.put("roleCode", "HACKER");
        boolean isTampered = SignatureUtil.verifySignature(requestParams, timestamp, signature);
        assertFalse(isTampered);
    }
} 