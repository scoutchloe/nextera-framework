package com.nextera.managenextera.filter;

import com.nextera.managenextera.interceptor.CachedBodyHttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Content-Type处理测试
 */
public class ContentTypeTest {

    @Test
    public void testOriginalRequestContentType() throws Exception {
        // 创建原始请求
        MockHttpServletRequest originalRequest = new MockHttpServletRequest();
        originalRequest.setContentType("application/json;charset=UTF-8");
        originalRequest.setContent("{\"test\":\"data\"}".getBytes());

        // 包装为缓存请求
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(originalRequest);

        // 验证Content-Type保持不变
        assertEquals("application/json;charset=UTF-8", cachedRequest.getContentType());
    }

    @Test
    public void testDecryptedRequestContentType() {
        // 创建原始请求（text/plain）
        MockHttpServletRequest originalRequest = new MockHttpServletRequest();
        originalRequest.setContentType("text/plain;charset=UTF-8");
        originalRequest.addHeader("Content-Type", "text/plain;charset=UTF-8");
        
        // 创建解密后的请求包装器
        String decryptedBody = "{\"id\":1,\"name\":\"测试角色\"}";
        CachedBodyHttpServletRequest decryptedRequest = new CachedBodyHttpServletRequest(originalRequest, decryptedBody);
        
        // 验证Content-Type被正确重写
        assertEquals("application/json;charset=UTF-8", decryptedRequest.getContentType());
        assertEquals("application/json;charset=UTF-8", decryptedRequest.getHeader("Content-Type"));
        assertEquals("application/json;charset=UTF-8", decryptedRequest.getHeader("content-type"));
        
        // 验证Content-Length被正确设置
        assertEquals(decryptedBody.getBytes().length, decryptedRequest.getContentLength());
        assertEquals(decryptedBody.getBytes().length, decryptedRequest.getContentLengthLong());
        
        // 验证Body内容正确
        assertEquals(decryptedBody, decryptedRequest.getBody());
    }

    @Test
    public void testNormalTextPlainRequest() throws Exception {
        // 创建普通的text/plain请求（非解密请求）
        MockHttpServletRequest originalRequest = new MockHttpServletRequest();
        originalRequest.setContentType("text/plain;charset=UTF-8");
        originalRequest.setContent("normal text content".getBytes());

        // 包装为普通缓存请求
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(originalRequest);

        // 验证Content-Type保持为text/plain
        assertEquals("text/plain;charset=UTF-8", cachedRequest.getContentType());
    }

    @Test
    public void testNormalRequestContentType() throws Exception {
        // 创建普通请求
        MockHttpServletRequest originalRequest = new MockHttpServletRequest();
        originalRequest.setContentType("application/json;charset=UTF-8");
        originalRequest.addHeader("Content-Type", "application/json;charset=UTF-8");
        originalRequest.setContent("{\"test\":\"data\"}".getBytes());
        
        // 创建普通的缓存请求包装器
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(originalRequest);
        
        // 验证Content-Type保持原样
        assertEquals("application/json;charset=UTF-8", cachedRequest.getContentType());
        assertEquals("application/json;charset=UTF-8", cachedRequest.getHeader("Content-Type"));
    }
    
    @Test
    public void testHeaderCaseInsensitive() {
        // 创建原始请求
        MockHttpServletRequest originalRequest = new MockHttpServletRequest();
        originalRequest.setContentType("text/plain;charset=UTF-8");
        originalRequest.addHeader("Content-Type", "text/plain;charset=UTF-8");
        
        // 创建解密后的请求包装器
        String decryptedBody = "{\"id\":1,\"name\":\"测试角色\"}";
        CachedBodyHttpServletRequest decryptedRequest = new CachedBodyHttpServletRequest(originalRequest, decryptedBody);
        
        // 验证各种大小写的Header都被正确重写
        assertEquals("application/json;charset=UTF-8", decryptedRequest.getHeader("Content-Type"));
        assertEquals("application/json;charset=UTF-8", decryptedRequest.getHeader("content-type"));
        assertEquals("application/json;charset=UTF-8", decryptedRequest.getHeader("CONTENT-TYPE"));
        assertEquals("application/json;charset=UTF-8", decryptedRequest.getHeader("Content-type"));
    }
    
    @Test
    public void testOtherHeadersUnaffected() {
        // 创建原始请求
        MockHttpServletRequest originalRequest = new MockHttpServletRequest();
        originalRequest.setContentType("text/plain;charset=UTF-8");
        originalRequest.addHeader("Content-Type", "text/plain;charset=UTF-8");
        originalRequest.addHeader("Authorization", "Bearer token123");
        originalRequest.addHeader("X-Custom-Header", "custom-value");
        
        // 创建解密后的请求包装器
        String decryptedBody = "{\"id\":1,\"name\":\"测试角色\"}";
        CachedBodyHttpServletRequest decryptedRequest = new CachedBodyHttpServletRequest(originalRequest, decryptedBody);
        
        // 验证只有Content-Type被重写，其他Header保持不变
        assertEquals("application/json;charset=UTF-8", decryptedRequest.getHeader("Content-Type"));
        assertEquals("Bearer token123", decryptedRequest.getHeader("Authorization"));
        assertEquals("custom-value", decryptedRequest.getHeader("X-Custom-Header"));
    }
} 