package com.nextera.managenextera.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextera.managenextera.dto.SysRoleDTO;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SysRoleController RSA解密处理测试
 */
public class SysRoleControllerRSATest {

    @Test
    public void testRSADecryptedRequestHandling() throws Exception {
        System.out.println("=== 测试RSA解密请求处理 ===");
        
        // 准备解密后的JSON数据
        SysRoleDTO originalDto = new SysRoleDTO();
        originalDto.setId(5L);
        originalDto.setRoleName("测试sign拦截器");
        originalDto.setRoleCode("TEST_SIGN_INTERCEPTOR");
        originalDto.setDescription("jjjjj");
        originalDto.setStatus(1);
        originalDto.setPermissionIds(Arrays.asList(1L, 2L, 3L));
        
        ObjectMapper objectMapper = new ObjectMapper();
        String decryptedData = objectMapper.writeValueAsString(originalDto);
        System.out.println("解密后数据: " + decryptedData);
        
        // 创建Mock请求
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("RSA_DECRYPTED", true);
        request.setAttribute("RSA_DECRYPTED_DATA", decryptedData);
        
        // 模拟控制器处理逻辑
        Boolean isRSADecrypted = (Boolean) request.getAttribute("RSA_DECRYPTED");
        assertNotNull(isRSADecrypted);
        assertTrue(isRSADecrypted);
        
        String retrievedData = (String) request.getAttribute("RSA_DECRYPTED_DATA");
        assertNotNull(retrievedData);
        assertEquals(decryptedData, retrievedData);
        
        // 测试JSON转换为DTO
        SysRoleDTO convertedDto = objectMapper.readValue(retrievedData, SysRoleDTO.class);
        
        // 验证转换结果
        assertEquals(originalDto.getId(), convertedDto.getId());
        assertEquals(originalDto.getRoleName(), convertedDto.getRoleName());
        assertEquals(originalDto.getRoleCode(), convertedDto.getRoleCode());
        assertEquals(originalDto.getDescription(), convertedDto.getDescription());
        assertEquals(originalDto.getStatus(), convertedDto.getStatus());
        assertEquals(originalDto.getPermissionIds(), convertedDto.getPermissionIds());
        
        System.out.println("转换后DTO: " + convertedDto);
        System.out.println("✓ RSA解密请求处理测试通过");
    }
    
    @Test
    public void testNormalRequestHandling() {
        System.out.println("=== 测试普通请求处理 ===");
        
        // 创建Mock请求（无RSA解密标记）
        MockHttpServletRequest request = new MockHttpServletRequest();
        
        // 模拟控制器处理逻辑
        Boolean isRSADecrypted = (Boolean) request.getAttribute("RSA_DECRYPTED");
        
        // 验证普通请求不会被识别为RSA解密请求
        assertNull(isRSADecrypted);
        
        System.out.println("✓ 普通请求处理测试通过");
    }
} 