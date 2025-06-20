package com.nextera.managenextera.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextera.managenextera.util.SignatureUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * 签名拦截器测试类
 */
public class SignatureInterceptorTest {

    private SignatureInterceptor signatureInterceptor;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        signatureInterceptor = new SignatureInterceptor();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testNonTargetUrl() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/other/api");
        request.setMethod("POST");
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        boolean result = signatureInterceptor.preHandle(request, response, null);
        
        assertTrue(result); // 非目标接口应该直接放行
    }

    @Test
    public void testTargetUrlWithoutSignature() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/system/role");
        request.setMethod("POST");
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        boolean result = signatureInterceptor.preHandle(request, response, null);
        
        assertFalse(result); // 缺少签名应该被拦截
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testValidSignature() throws Exception {
        // 准备测试数据
        Map<String, Object> roleData = new HashMap<>();
        roleData.put("roleCode", "TEST_ROLE");
        roleData.put("roleName", "测试角色");
        roleData.put("status", 1);
        roleData.put("description", "测试描述");
        
        long timestamp = System.currentTimeMillis();
        String signature = SignatureUtil.generateSignature(roleData, timestamp);
        
        // 创建请求
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/system/role");
        request.setMethod("POST");
        request.addHeader("X-Signature", signature);
        request.addHeader("X-Timestamp", String.valueOf(timestamp));
        
        // 设置请求体
        String requestBody = objectMapper.writeValueAsString(roleData);
        request.setContent(requestBody.getBytes());
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        boolean result = signatureInterceptor.preHandle(request, response, null);
        
        assertTrue(result); // 有效签名应该通过验证
    }
    
    @Test
    public void testInvalidSignature() throws Exception {
        // 准备测试数据
        Map<String, Object> roleData = new HashMap<>();
        roleData.put("roleCode", "TEST_ROLE");
        roleData.put("roleName", "测试角色");
        
        long timestamp = System.currentTimeMillis();
        
        // 创建请求
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/system/role");
        request.setMethod("POST");
        request.addHeader("X-Signature", "INVALID_SIGNATURE");
        request.addHeader("X-Timestamp", String.valueOf(timestamp));
        
        // 设置请求体
        String requestBody = objectMapper.writeValueAsString(roleData);
        request.setContent(requestBody.getBytes());
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        boolean result = signatureInterceptor.preHandle(request, response, null);
        
        assertFalse(result); // 无效签名应该被拦截
        assertEquals(400, response.getStatus());
    }
} 