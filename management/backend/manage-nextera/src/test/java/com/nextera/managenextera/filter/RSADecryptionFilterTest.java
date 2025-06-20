package com.nextera.managenextera.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextera.managenextera.dto.SysRoleDTO;
import com.nextera.managenextera.util.RSAUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * RSA解密过滤器测试
 */
public class RSADecryptionFilterTest {

    private RSADecryptionFilter filter;
    private FilterChain filterChain;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    public void setUp() {
        filter = new RSADecryptionFilter();
        filterChain = mock(FilterChain.class);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    public void testNonTargetRequestPassthrough() throws Exception {
        // 设置非目标请求
        request.setMethod("GET");
        request.setRequestURI("/system/user");

        // 执行过滤器
        filter.doFilter(request, response, filterChain);

        // 验证原始请求被传递
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void testTargetRequestWithoutEncryption() throws Exception {
        // 设置目标请求但未加密
        request.setMethod("PUT");
        request.setRequestURI("/system/role");

        // 执行过滤器
        filter.doFilter(request, response, filterChain);

        // 验证原始请求被传递
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void testApiPrefixTargetRequestWithoutEncryption() throws Exception {
        // 设置带/api前缀的目标请求但未加密
        request.setMethod("PUT");
        request.setRequestURI("/api/system/role");

        // 执行过滤器
        filter.doFilter(request, response, filterChain);

        // 验证原始请求被传递
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void testEncryptedRequestDecryption() throws Exception {
        // 准备测试数据
        String originalData = "{\"id\":1,\"roleName\":\"测试角色\"}";
        String encryptedData = RSAUtil.encrypt(originalData, RSAUtil.getPublicKey());

        // 设置加密请求
        request.setMethod("PUT");
        request.setRequestURI("/system/role");
        request.addHeader("X-Encrypted", "true");
        request.setContentType("text/plain;charset=UTF-8");
        request.setContent(encryptedData.getBytes());

        // 执行过滤器
        filter.doFilter(request, response, filterChain);

        // 验证解密后的请求被传递
        ArgumentCaptor<ServletRequest> requestCaptor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(filterChain).doFilter(requestCaptor.capture(), eq(response));

        // 验证传递的不是原始请求
        assertNotSame(request, requestCaptor.getValue());
    }

    @Test
    public void testApiPrefixEncryptedRequestDecryption() throws Exception {
        // 准备测试数据
        String originalData = "{\"id\":1,\"roleName\":\"测试角色\"}";
        String encryptedData = RSAUtil.encrypt(originalData, RSAUtil.getPublicKey());

        // 设置带/api前缀的加密请求
        request.setMethod("PUT");
        request.setRequestURI("/api/system/role");
        request.addHeader("X-Encrypted", "true");
        request.setContentType("text/plain;charset=UTF-8");
        request.setContent(encryptedData.getBytes());

        // 执行过滤器
        filter.doFilter(request, response, filterChain);

        // 验证解密后的请求被传递
        ArgumentCaptor<ServletRequest> requestCaptor = ArgumentCaptor.forClass(ServletRequest.class);
        verify(filterChain).doFilter(requestCaptor.capture(), eq(response));

        // 验证传递的不是原始请求
        assertNotSame(request, requestCaptor.getValue());
    }

    @Test
    public void testInvalidEncryptedData() throws Exception {
        // 设置无效的加密请求
        request.setMethod("PUT");
        request.setRequestURI("/api/system/role");
        request.addHeader("X-Encrypted", "true");
        request.setContentType("text/plain;charset=UTF-8");
        request.setContent("invalid_encrypted_data".getBytes());

        // 执行过滤器
        filter.doFilter(request, response, filterChain);

        // 验证过滤器链没有被调用（因为解密失败）
        verify(filterChain, never()).doFilter(any(), any());

        // 验证返回了错误响应
        assertEquals(400, response.getStatus());
    }

    @Test
    public void testPathMatching() throws Exception {
        // 测试各种路径的匹配情况
        
        // 1. 测试 /system/role 路径
        request.setMethod("PUT");
        request.setRequestURI("/system/role");
        request.addHeader("X-Encrypted", "true");
        request.setContent("test".getBytes());
        
        filter.doFilter(request, response, filterChain);
        
        // 应该被处理（即使解密失败也会进入处理逻辑）
        verify(filterChain, never()).doFilter(request, response);
        
        // 重置mock
        reset(filterChain);
        response = new MockHttpServletResponse();
        
        // 2. 测试 /api/system/role 路径
        request.setRequestURI("/api/system/role");
        
        filter.doFilter(request, response, filterChain);
        
        // 应该被处理（即使解密失败也会进入处理逻辑）
        verify(filterChain, never()).doFilter(request, response);
        
        // 重置mock
        reset(filterChain);
        response = new MockHttpServletResponse();
        
        // 3. 测试其他路径不被处理
        request.setRequestURI("/other/path");
        
        filter.doFilter(request, response, filterChain);
        
        // 应该直接传递
        verify(filterChain).doFilter(request, response);
    }
} 