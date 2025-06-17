package com.nextera.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextera.auth.BaseTest;
import com.nextera.auth.dto.LoginRequest;
import com.nextera.auth.dto.RegisterRequest;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController集成测试
 *
 * @author Nextera
 */
@DisplayName("认证控制器测试")
public class AuthControllerTest extends BaseTest {

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
    @DisplayName("用户登录接口测试 - 成功")
    void testLoginSuccess() throws Exception {
        // 准备测试数据
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
        loginRequest.setCaptcha("1234");
        loginRequest.setUuid("test-uuid");

        // Mock服务返回
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(Result.success(createMockLoginResponse()));

        // 执行请求
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    @Test
    @DisplayName("用户登录接口测试 - 参数验证失败")
    void testLoginValidationFail() throws Exception {
        // 准备无效的测试数据
        LoginRequest loginRequest = new LoginRequest();
        // username为空

        // 执行请求
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("用户注册接口测试 - 成功")
    void testRegisterSuccess() throws Exception {
        // 准备测试数据
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setConfirmPassword("password123");
        registerRequest.setNickname("New User");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setCaptcha("1234");
        registerRequest.setUuid("test-uuid");

        // Mock服务返回
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(Result.success());

        // 执行请求
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("success"));
    }

    @Test
    @DisplayName("获取验证码接口测试")
    void testGetCaptcha() throws Exception {
        // Mock服务返回
        when(authService.getCaptcha(any(String.class)))
                .thenReturn(Result.success("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."));

        // 执行请求
        mockMvc.perform(get("/auth/captcha"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("验证Token接口测试")
    void testValidateToken() throws Exception {
        // Mock服务返回
        when(authService.validateToken("valid-token"))
                .thenReturn(Result.success(true));

        // 执行请求
        mockMvc.perform(get("/auth/validate")
                        .header("Authorization", "Bearer valid-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("用户登出接口测试")
    void testLogout() throws Exception {
        // Mock服务返回
        when(authService.logout("valid-token"))
                .thenReturn(Result.success());

        // 执行请求
        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", "Bearer valid-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("刷新Token接口测试")
    void testRefreshToken() throws Exception {
        // Mock服务返回
        when(authService.refreshToken("refresh-token"))
                .thenReturn(Result.success(createMockLoginResponse()));

        // 执行请求
        mockMvc.perform(post("/auth/refresh")
                        .param("refreshToken", "refresh-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.accessToken").exists());
    }

    @Test
    @DisplayName("健康检查接口测试")
    void testHealth() throws Exception {
        // 执行请求
        mockMvc.perform(get("/auth/health"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("Auth Service is running"));
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