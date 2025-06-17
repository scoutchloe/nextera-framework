package com.nextera.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextera.api.auth.dto.AuthTokenDTO;
import com.nextera.api.auth.dto.LoginRequest;
import com.nextera.api.auth.dto.RegisterRequest;
import com.nextera.auth.BaseTest;
import com.nextera.auth.service.AuthService;
import com.nextera.common.core.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthInternalController测试
 *
 * @author Nextera
 */
@DisplayName("认证内部API控制器测试")
public class AuthInternalControllerTest extends BaseTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private AuthService authService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("内部登录接口测试 - 成功")
    void testInternalLoginSuccess() throws Exception {
        // 准备测试数据
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        // Mock服务返回
        com.nextera.auth.dto.LoginResponse mockLoginResponse = createMockLoginResponse();
        when(authService.loginInner(any(LoginRequest.class)))
                .thenReturn(Result.success(mockLoginResponse));

        // 执行请求
        mockMvc.perform(post("/internal/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("mock-refresh-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("内部登录接口测试 - 失败")
    void testInternalLoginFail() throws Exception {
        // 准备测试数据
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        // Mock服务返回失败
        when(authService.loginInner(any(LoginRequest.class)))
                .thenReturn(Result.error("登录失败"));

        // 执行请求
        mockMvc.perform(post("/internal/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("登录失败"));
    }

    @Test
    @DisplayName("内部注册接口测试 - 成功")
    void testInternalRegisterSuccess() throws Exception {
        // 准备测试数据
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("newuser@example.com");

        // Mock服务返回
        when(authService.registerInner(any(RegisterRequest.class)))
                .thenReturn(Result.success());

        // 执行请求
        mockMvc.perform(post("/internal/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("内部登出接口测试")
    void testInternalLogout() throws Exception {
        // Mock服务返回
        when(authService.logout(anyString()))
                .thenReturn(Result.success());

        // 执行请求
        mockMvc.perform(post("/internal/auth/logout")
                        .param("token", "valid-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("内部刷新令牌接口测试")
    void testInternalRefreshToken() throws Exception {
        // Mock服务返回
        com.nextera.auth.dto.LoginResponse mockLoginResponse = createMockLoginResponse();
        when(authService.refreshToken(anyString()))
                .thenReturn(Result.success(mockLoginResponse));

        // 执行请求
        mockMvc.perform(post("/internal/auth/refresh")
                        .param("refreshToken", "refresh-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("mock-access-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("内部验证令牌接口测试 - 成功")
    void testInternalValidateTokenSuccess() throws Exception {
        // Mock服务返回
        when(authService.validateToken(anyString()))
                .thenReturn(Result.success(true));

        // 执行请求
        mockMvc.perform(get("/internal/auth/validate")
                        .param("token", "valid-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").value("valid-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("内部验证令牌接口测试 - 失败")
    void testInternalValidateTokenFail() throws Exception {
        // Mock服务返回
        when(authService.validateToken(anyString()))
                .thenReturn(Result.success(false));

        // 执行请求
        mockMvc.perform(get("/internal/auth/validate")
                        .param("token", "invalid-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("Token验证失败"));
    }

    @Test
    @DisplayName("内部获取验证码接口测试")
    void testInternalGetCaptcha() throws Exception {
        // Mock服务返回
        when(authService.getCaptcha(anyString()))
                .thenReturn(Result.success("data:image/png;base64,mock-captcha"));

        // 执行请求
        mockMvc.perform(get("/internal/auth/captcha"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("data:image/png;base64,mock-captcha"));
    }

    @Test
    @DisplayName("内部发送邮箱验证码接口测试")
    void testInternalSendEmailCode() throws Exception {
        // 执行请求
        mockMvc.perform(post("/internal/auth/email/send")
                        .param("email", "test@example.com"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("内部重置密码接口测试")
    void testInternalResetPassword() throws Exception {
        // 执行请求
        mockMvc.perform(post("/internal/auth/password/reset")
                        .param("email", "test@example.com")
                        .param("emailCode", "123456")
                        .param("newPassword", "newpassword123"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("内部修改密码接口测试")
    void testInternalChangePassword() throws Exception {
        // 执行请求
        mockMvc.perform(post("/internal/auth/password/change")
                        .param("userId", "1")
                        .param("oldPassword", "oldpassword")
                        .param("newPassword", "newpassword123"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("内部检查权限接口测试")
    void testInternalHasPermission() throws Exception {
        // 执行请求
        mockMvc.perform(get("/internal/auth/permission/check")
                        .param("userId", "1")
                        .param("permission", "user:read"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("内部检查角色接口测试")
    void testInternalHasRole() throws Exception {
        // 执行请求
        mockMvc.perform(get("/internal/auth/role/check")
                        .param("userId", "1")
                        .param("role", "USER"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    /**
     * 创建Mock登录响应
     */
    private com.nextera.auth.dto.LoginResponse createMockLoginResponse() {
        com.nextera.auth.dto.LoginResponse response = new com.nextera.auth.dto.LoginResponse();
        response.setAccessToken("mock-access-token");
        response.setRefreshToken("mock-refresh-token");
        response.setExpiresIn(24 * 60 * 60L);

        com.nextera.auth.dto.LoginResponse.UserInfo userInfo = new com.nextera.auth.dto.LoginResponse.UserInfo();
        userInfo.setId(1L);
        userInfo.setUsername("testuser");
        userInfo.setNickname("Test User");
        userInfo.setEmail("test@example.com");
        userInfo.setRoles(new String[]{"USER"});
        userInfo.setPermissions(new String[]{"user:read"});

        response.setUserInfo(userInfo);
        return response;
    }
} 