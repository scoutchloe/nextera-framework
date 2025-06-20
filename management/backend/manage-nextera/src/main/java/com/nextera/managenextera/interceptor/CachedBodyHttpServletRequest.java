package com.nextera.managenextera.interceptor;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 缓存请求体的HttpServletRequest包装类
 * 允许多次读取请求体
 */
@Slf4j
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    private final byte[] cachedBody;
    private final boolean isDecryptedRequest;

    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        this.isDecryptedRequest = false;
        
        // 读取并缓存请求体
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = request.getReader()) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        
        this.cachedBody = stringBuilder.toString().getBytes(StandardCharsets.UTF_8);
    }
    
    /**
     * 用指定的body内容构造包装器（用于解密后的数据）
     */
    public CachedBodyHttpServletRequest(HttpServletRequest request, String bodyContent) {
        super(request);
        this.isDecryptedRequest = true;
        this.cachedBody = bodyContent.getBytes(StandardCharsets.UTF_8);
    }
    
    @Override
    public String getContentType() {
        // 如果是解密后的请求，强制设置为JSON格式
        if (isDecryptedRequest) {
            log.debug("返回解密请求的Content-Type: application/json;charset=UTF-8");
            return "application/json;charset=UTF-8";
        }
        return super.getContentType();
    }
    
    @Override
    public String getHeader(String name) {
        // 对于解密请求，重写Content-Type相关的Header
        if (isDecryptedRequest && "Content-Type".equalsIgnoreCase(name)) {
            log.debug("返回解密请求的Header Content-Type: application/json;charset=UTF-8");
            return "application/json;charset=UTF-8";
        }
        return super.getHeader(name);
    }
    
    @Override
    public java.util.Enumeration<String> getHeaders(String name) {
        // 对于解密请求，重写Content-Type相关的Headers
        if (isDecryptedRequest && "Content-Type".equalsIgnoreCase(name)) {
            log.debug("返回解密请求的Headers Content-Type: application/json;charset=UTF-8");
            return java.util.Collections.enumeration(java.util.Arrays.asList("application/json;charset=UTF-8"));
        }
        return super.getHeaders(name);
    }
    
    @Override
    public java.util.Enumeration<String> getHeaderNames() {
        if (isDecryptedRequest) {
            // 获取原始header names，但确保Content-Type被正确处理
            java.util.Set<String> headerNames = new java.util.HashSet<>();
            java.util.Enumeration<String> originalNames = super.getHeaderNames();
            while (originalNames.hasMoreElements()) {
                headerNames.add(originalNames.nextElement());
            }
            // 确保Content-Type在header names中
            headerNames.add("Content-Type");
            log.debug("返回解密请求的HeaderNames，包含Content-Type");
            return java.util.Collections.enumeration(headerNames);
        }
        return super.getHeaderNames();
    }
    
    @Override
    public int getContentLength() {
        // 如果是解密后的请求，返回实际内容长度
        if (isDecryptedRequest) {
            return cachedBody.length;
        }
        return super.getContentLength();
    }
    
    @Override
    public long getContentLengthLong() {
        // 如果是解密后的请求，返回实际内容长度
        if (isDecryptedRequest) {
            return (long) cachedBody.length;
        }
        return super.getContentLengthLong();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new CachedBodyServletInputStream(this.cachedBody);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedBody);
        return new BufferedReader(new InputStreamReader(byteArrayInputStream, StandardCharsets.UTF_8));
    }

    /**
     * 获取缓存的请求体字符串
     */
    public String getBody() {
        return new String(this.cachedBody, StandardCharsets.UTF_8);
    }

    /**
     * 自定义ServletInputStream实现
     */
    private static class CachedBodyServletInputStream extends ServletInputStream {

        private final ByteArrayInputStream byteArrayInputStream;

        public CachedBodyServletInputStream(byte[] cachedBody) {
            this.byteArrayInputStream = new ByteArrayInputStream(cachedBody);
        }

        @Override
        public boolean isFinished() {
            return byteArrayInputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            // Not implemented
        }

        @Override
        public int read() throws IOException {
            return byteArrayInputStream.read();
        }
    }
} 