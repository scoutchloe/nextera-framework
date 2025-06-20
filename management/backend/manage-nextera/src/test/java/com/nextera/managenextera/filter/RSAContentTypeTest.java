package com.nextera.managenextera.filter;

import com.nextera.managenextera.interceptor.CachedBodyHttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RSA解密Content-Type处理专项测试
 * 针对用户遇到的"Content-Type 'text/plain;charset=UTF-8' is not supported"问题
 */
public class RSAContentTypeTest {

    @Test
    public void testRSADecryptionContentTypeConversion() {
        System.out.println("=== 测试RSA解密请求的Content-Type转换 ===");
        
        // 模拟前端发送的RSA加密请求
        MockHttpServletRequest originalRequest = new MockHttpServletRequest();
        originalRequest.setMethod("PUT");
        originalRequest.setRequestURI("/system/role");
        originalRequest.setContentType("text/plain;charset=UTF-8");
        originalRequest.addHeader("Content-Type", "text/plain;charset=UTF-8");
        originalRequest.addHeader("X-Encrypted", "true");
        
        System.out.println("原始请求Content-Type: " + originalRequest.getContentType());
        System.out.println("原始请求Header Content-Type: " + originalRequest.getHeader("Content-Type"));
        
        // 模拟解密后的JSON数据
        String decryptedJsonData = "{\"id\":1,\"roleName\":\"测试角色\",\"description\":\"这是一个测试角色\"}";
        
        // 创建解密后的请求包装器
        CachedBodyHttpServletRequest decryptedRequest = new CachedBodyHttpServletRequest(originalRequest, decryptedJsonData);
        
        // 验证Content-Type被正确转换
        System.out.println("解密后请求Content-Type: " + decryptedRequest.getContentType());
        System.out.println("解密后请求Header Content-Type: " + decryptedRequest.getHeader("Content-Type"));
        
        // 断言验证
        assertEquals("application/json;charset=UTF-8", decryptedRequest.getContentType(), 
                    "解密后的请求Content-Type应该被转换为application/json");
        assertEquals("application/json;charset=UTF-8", decryptedRequest.getHeader("Content-Type"), 
                    "解密后的请求Header Content-Type应该被转换为application/json");
        
        // 验证内容长度
        assertEquals(decryptedJsonData.getBytes().length, decryptedRequest.getContentLength(),
                    "Content-Length应该与解密后数据长度一致");
        assertEquals((long) decryptedJsonData.getBytes().length, decryptedRequest.getContentLengthLong(),
                    "Content-Length-Long应该与解密后数据长度一致");
        
        // 验证解密后的数据内容
        assertEquals(decryptedJsonData, decryptedRequest.getBody(),
                    "解密后的数据内容应该正确");
        
        System.out.println("✓ RSA解密Content-Type转换测试通过");
    }
    
    @Test
    public void testOriginalRequestUnchanged() {
        System.out.println("=== 测试原始请求保持不变 ===");
        
        // 创建普通JSON请求
        MockHttpServletRequest originalRequest = new MockHttpServletRequest();
        originalRequest.setMethod("PUT");
        originalRequest.setRequestURI("/system/role");
        originalRequest.setContentType("application/json;charset=UTF-8");
        originalRequest.addHeader("Content-Type", "application/json;charset=UTF-8");
        originalRequest.setContent("{\"id\":1,\"roleName\":\"普通角色\"}".getBytes());
        
        try {
            // 创建普通的缓存请求包装器（非解密）
            CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(originalRequest);
            
            // 验证Content-Type保持原样
            assertEquals("application/json;charset=UTF-8", cachedRequest.getContentType(),
                        "普通请求的Content-Type应该保持不变");
            assertEquals("application/json;charset=UTF-8", cachedRequest.getHeader("Content-Type"),
                        "普通请求的Header Content-Type应该保持不变");
            
            System.out.println("✓ 普通请求Content-Type保持不变测试通过");
        } catch (Exception e) {
            fail("处理普通请求时不应该出现异常: " + e.getMessage());
        }
    }
    
    @Test
    public void testMultipleHeaderAccess() {
        System.out.println("=== 测试多次访问Header的一致性 ===");
        
        // 创建加密请求
        MockHttpServletRequest originalRequest = new MockHttpServletRequest();
        originalRequest.setContentType("text/plain;charset=UTF-8");
        originalRequest.addHeader("Content-Type", "text/plain;charset=UTF-8");
        
        String decryptedData = "{\"test\":\"data\"}";
        CachedBodyHttpServletRequest decryptedRequest = new CachedBodyHttpServletRequest(originalRequest, decryptedData);
        
        // 多次调用应该返回相同结果
        String contentType1 = decryptedRequest.getContentType();
        String contentType2 = decryptedRequest.getContentType();
        String headerType1 = decryptedRequest.getHeader("Content-Type");
        String headerType2 = decryptedRequest.getHeader("Content-Type");
        
        assertEquals(contentType1, contentType2, "多次调用getContentType()应该返回相同结果");
        assertEquals(headerType1, headerType2, "多次调用getHeader()应该返回相同结果");
        assertEquals(contentType1, headerType1, "getContentType()和getHeader()应该返回相同结果");
        
        System.out.println("✓ 多次访问Header一致性测试通过");
    }
    
    @Test
    public void testSpringCompatibility() {
        System.out.println("=== 测试Spring兼容性 ===");
        
        // 模拟Spring处理请求的场景
        MockHttpServletRequest originalRequest = new MockHttpServletRequest();
        originalRequest.setMethod("PUT");
        originalRequest.setRequestURI("/system/role");
        originalRequest.setContentType("text/plain;charset=UTF-8");
        originalRequest.addHeader("Content-Type", "text/plain;charset=UTF-8");
        originalRequest.addHeader("X-Encrypted", "true");
        
        String roleData = "{\"id\":1,\"roleName\":\"管理员角色\",\"description\":\"系统管理员角色\",\"status\":1}";
        CachedBodyHttpServletRequest decryptedRequest = new CachedBodyHttpServletRequest(originalRequest, roleData);
        
        // 验证Spring需要的关键属性
        assertEquals("application/json;charset=UTF-8", decryptedRequest.getContentType());
        assertEquals("application/json;charset=UTF-8", decryptedRequest.getHeader("Content-Type"));
        assertEquals("application/json;charset=UTF-8", decryptedRequest.getHeader("content-type"));
        assertEquals("PUT", decryptedRequest.getMethod());
        assertEquals("/system/role", decryptedRequest.getRequestURI());
        assertEquals(roleData.getBytes().length, decryptedRequest.getContentLength());
        
        // 验证能够读取到正确的数据
        assertEquals(roleData, decryptedRequest.getBody());
        
        System.out.println("Content-Type: " + decryptedRequest.getContentType());
        System.out.println("Method: " + decryptedRequest.getMethod());
        System.out.println("URI: " + decryptedRequest.getRequestURI());
        System.out.println("Content-Length: " + decryptedRequest.getContentLength());
        System.out.println("Body: " + decryptedRequest.getBody());
        
        System.out.println("✓ Spring兼容性测试通过");
    }
} 