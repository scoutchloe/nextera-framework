package com.nextera.auth.controller;

import cn.hutool.core.util.IdUtil;
import com.nextera.auth.dto.LoginRequest;
import com.nextera.auth.dto.LoginResponse;
import com.nextera.auth.dto.RegisterRequest;
import com.nextera.auth.service.AuthService;
import com.nextera.common.core.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 认证控制器
 *
 * @author Nextera
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户认证相关接口")
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录接口")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest,
                                     HttpServletRequest request) {
        String clientIp = getClientIp(request);
        log.info("用户登录请求: {}, IP: {}", loginRequest.getUsername(), clientIp);
        return authService.login(loginRequest);
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户注册接口")
    public Result<Void> register(@Valid @RequestBody RegisterRequest registerRequest,
                               HttpServletRequest request) {
        String clientIp = getClientIp(request);
        log.info("用户注册请求: {}, IP: {}", registerRequest.getUsername(), clientIp);
        return authService.register(registerRequest);
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新Token", description = "使用刷新Token获取新的访问Token")
    public Result<LoginResponse> refreshToken(@Parameter(description = "刷新Token") 
                                            @RequestParam String refreshToken) {
        return authService.refreshToken(refreshToken);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户退出", description = "用户退出登录接口")
    public Result<Void> logout(@Parameter(description = "访问Token") 
                             @RequestHeader("Authorization") String token) {
        // 移除Bearer前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return authService.logout(token);
    }

    @GetMapping("/validate")
    @Operation(summary = "验证Token", description = "验证Token有效性")
    public Result<Boolean> validateToken(@Parameter(description = "访问Token") 
                                       @RequestHeader("Authorization") String token) {
        // 移除Bearer前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return authService.validateToken(token);
    }

    @GetMapping("/captcha")
    @Operation(summary = "获取验证码", description = "获取图形验证码")
    public Result<String> getCaptcha() {
        String uuid = IdUtil.simpleUUID();
        Result<String> result = authService.getCaptcha(uuid);
        if (result.isSuccess()) {
            // 将UUID添加到响应中
            return Result.success(result.getData());
        }
        return result;
    }

    @GetMapping("/captcha/{uuid}")
    @Operation(summary = "获取指定UUID的验证码", description = "根据UUID获取图形验证码")
    public Result<String> getCaptcha(@Parameter(description = "验证码唯一标识") 
                                   @PathVariable String uuid) {
        return authService.getCaptcha(uuid);
    }

    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "认证服务健康检查接口")
    public Result<String> health() {
        return Result.success("Auth Service is running");
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        String proxyClientIp = request.getHeader("Proxy-Client-IP");
        if (proxyClientIp != null && !proxyClientIp.isEmpty() && !"unknown".equalsIgnoreCase(proxyClientIp)) {
            return proxyClientIp;
        }
        
        String wlProxyClientIp = request.getHeader("WL-Proxy-Client-IP");
        if (wlProxyClientIp != null && !wlProxyClientIp.isEmpty() && !"unknown".equalsIgnoreCase(wlProxyClientIp)) {
            return wlProxyClientIp;
        }
        
        String httpClientIp = request.getHeader("HTTP_CLIENT_IP");
        if (httpClientIp != null && !httpClientIp.isEmpty() && !"unknown".equalsIgnoreCase(httpClientIp)) {
            return httpClientIp;
        }
        
        String httpXForwardedFor = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (httpXForwardedFor != null && !httpXForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(httpXForwardedFor)) {
            return httpXForwardedFor;
        }
        
        return request.getRemoteAddr();
    }
} 