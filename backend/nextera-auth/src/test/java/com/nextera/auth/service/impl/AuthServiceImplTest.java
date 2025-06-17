package com.nextera.auth.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.nextera.api.auth.dto.LoginRequest;
import com.nextera.api.auth.dto.RegisterRequest;
import com.nextera.auth.BaseTest;
import com.nextera.auth.entity.User;
import com.nextera.auth.mapper.UserMapper;
import com.nextera.auth.service.AuthService;
import com.nextera.common.constant.CommonConstants;
import com.nextera.common.core.Result;
import com.nextera.common.core.ResultCode;
import com.nextera.common.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * AuthService实现类测试
 *
 * @author Nextera
 */
@DisplayName("认证服务测试")
public class AuthServiceImplTest extends BaseTest {

    @Autowired
    private AuthService authService;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private StringRedisTemplate redisTemplate;

    @MockBean
    private ValueOperations<String, String> valueOperations;

    private User testUser;
    private com.nextera.auth.dto.LoginRequest loginRequest;
    private com.nextera.auth.dto.RegisterRequest registerRequest;

    @BeforeEach
    void initTestData() {
        // 初始化测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword(BCrypt.hashpw("password123", BCrypt.gensalt()));
        testUser.setNickname("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPhone("13800138000");
        testUser.setStatus(CommonConstants.UserStatus.NORMAL);
        testUser.setCreateTime(LocalDateTime.now());
        testUser.setUpdateTime(LocalDateTime.now());

        // 初始化登录请求
        loginRequest = new com.nextera.auth.dto.LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");
        loginRequest.setCaptcha("1234");

        // 初始化注册请求
        registerRequest = new com.nextera.auth.dto.RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setConfirmPassword("password123");
        registerRequest.setNickname("New User");
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPhone("13900139000");
        registerRequest.setCaptcha("1234");

        // Mock Redis操作
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("用户登录成功")
    void testLoginSuccess() {
        // Mock验证码验证
        when(valueOperations.get(CommonConstants.CachePrefix.CAPTCHA + "test-uuid"))
                .thenReturn("1234");
        
        // Mock用户查询
        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);
        
        // Mock JWT生成
        when(jwtUtil.generateToken(1L, "testuser")).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(1L, "testuser")).thenReturn("refresh-token");
        
        // Mock Redis操作
        doNothing().when(redisTemplate).delete(anyString());
        doNothing().when(valueOperations).set(anyString(), anyString(), any());
        
        // Mock用户更新
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        // 执行登录
        Result<com.nextera.auth.dto.LoginResponse> result = authService.login(loginRequest);

        // 验证结果
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("access-token", result.getData().getAccessToken());
        assertEquals("refresh-token", result.getData().getRefreshToken());
        assertEquals(24 * 60 * 60L, result.getData().getExpiresIn());

        // 验证方法调用
        verify(userMapper).selectByUsername("testuser");
        verify(jwtUtil).generateToken(1L, "testuser");
        verify(jwtUtil).generateRefreshToken(1L, "testuser");
        verify(userMapper).updateById(any(User.class));
    }

    @Test
    @DisplayName("用户登录失败 - 验证码错误")
    void testLoginFailWithWrongCaptcha() {
        // Mock验证码验证
        when(valueOperations.get(CommonConstants.CachePrefix.CAPTCHA + "test-uuid"))
                .thenReturn("5678");

        // 执行登录
        Result<com.nextera.auth.dto.LoginResponse> result = authService.login(loginRequest);

        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals(ResultCode.CAPTCHA_ERROR.getCode(), result.getCode());
    }

    @Test
    @DisplayName("用户登录失败 - 用户不存在")
    void testLoginFailWithUserNotFound() {
        // Mock验证码验证
        when(valueOperations.get(CommonConstants.CachePrefix.CAPTCHA + "test-uuid"))
                .thenReturn("1234");
        
        // Mock用户查询返回null
        when(userMapper.selectByUsername("testuser")).thenReturn(null);

        // 执行登录
        Result<com.nextera.auth.dto.LoginResponse> result = authService.login(loginRequest);

        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals(ResultCode.USER_NOT_FOUND.getCode(), result.getCode());
    }

    @Test
    @DisplayName("用户登录失败 - 密码错误")
    void testLoginFailWithWrongPassword() {
        // Mock验证码验证
        when(valueOperations.get(CommonConstants.CachePrefix.CAPTCHA + "test-uuid"))
                .thenReturn("1234");
        
        // Mock用户查询
        when(userMapper.selectByUsername("testuser")).thenReturn(testUser);
        
        // 设置错误密码
        loginRequest.setPassword("wrongpassword");

        // 执行登录
        Result<com.nextera.auth.dto.LoginResponse> result = authService.login(loginRequest);

        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals(ResultCode.LOGIN_FAILED.getCode(), result.getCode());
    }

    @Test
    @DisplayName("用户注册成功")
    void testRegisterSuccess() {
        // Mock验证码验证
        when(valueOperations.get(CommonConstants.CachePrefix.CAPTCHA + "test-uuid"))
                .thenReturn("1234");
        
        // Mock用户不存在检查
        when(userMapper.selectByUsername("newuser")).thenReturn(null);
        when(userMapper.selectByEmail("newuser@example.com")).thenReturn(null);
        when(userMapper.selectByPhone("13900139000")).thenReturn(null);
        
        // Mock用户插入
        when(userMapper.insert(any(User.class))).thenReturn(1);
        
        // Mock Redis操作
        doNothing().when(redisTemplate).delete(anyString());

        // 执行注册
        Result<Void> result = authService.register(registerRequest);

        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals(CommonConstants.REGISTER_SUCCESS, result.getMessage());

        // 验证方法调用
        verify(userMapper).selectByUsername("newuser");
        verify(userMapper).selectByEmail("newuser@example.com");
        verify(userMapper).selectByPhone("13900139000");
        verify(userMapper).insert(any(User.class));
    }

    @Test
    @DisplayName("用户注册失败 - 用户名已存在")
    void testRegisterFailWithUsernameExists() {
        // Mock验证码验证
        when(valueOperations.get(CommonConstants.CachePrefix.CAPTCHA + "test-uuid"))
                .thenReturn("1234");
        
        // Mock用户名已存在
        when(userMapper.selectByUsername("newuser")).thenReturn(testUser);

        // 执行注册
        Result<Void> result = authService.register(registerRequest);

        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals(ResultCode.USERNAME_EXISTS.getCode(), result.getCode());
    }

    @Test
    @DisplayName("用户注册失败 - 密码不一致")
    void testRegisterFailWithPasswordNotMatch() {
        // Mock验证码验证
        when(valueOperations.get(CommonConstants.CachePrefix.CAPTCHA + "test-uuid"))
                .thenReturn("1234");
        
        // 设置不一致的密码
        registerRequest.setConfirmPassword("differentpassword");

        // 执行注册
        Result<Void> result = authService.register(registerRequest);

        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals(ResultCode.PASSWORD_NOT_MATCH.getCode(), result.getCode());
    }

    @Test
    @DisplayName("令牌验证成功")
    void testValidateTokenSuccess() {
        String token = "valid-token";
        
        // Mock JWT验证
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getUserIdFromToken(token)).thenReturn(1L);
        
        // Mock Redis验证
        when(valueOperations.get(CommonConstants.CachePrefix.LOGIN_TOKEN + 1L))
                .thenReturn(token);

        // 执行验证
        Result<Boolean> result = authService.validateToken(token);

        // 验证结果
        assertTrue(result.isSuccess());
        assertTrue(result.getData());
    }

    @Test
    @DisplayName("令牌验证失败")
    void testValidateTokenFail() {
        String token = "invalid-token";
        
        // Mock JWT验证返回false
        when(jwtUtil.validateToken(token)).thenReturn(false);

        // 执行验证
        Result<Boolean> result = authService.validateToken(token);

        // 验证结果
        assertTrue(result.isSuccess());
        assertFalse(result.getData());
    }

    @Test
    @DisplayName("用户登出成功")
    void testLogoutSuccess() {
        String token = "valid-token";
        
        // Mock JWT验证
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.getUserIdFromToken(token)).thenReturn(1L);
        
        // Mock Redis删除
        doNothing().when(redisTemplate).delete(anyString());

        // 执行登出
        Result<Void> result = authService.logout(token);

        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals(CommonConstants.LOGOUT_SUCCESS, result.getMessage());

        // 验证Redis删除操作
        verify(redisTemplate, times(2)).delete(anyString());
    }
} 