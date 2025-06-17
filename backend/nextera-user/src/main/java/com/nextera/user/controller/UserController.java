package com.nextera.user.controller;

import com.nextera.api.user.service.UserService;
import com.nextera.common.core.Result;
import com.nextera.user.dto.UserInfoDTO;
import com.nextera.user.feign.AuthServiceClient;
import com.nextera.user.service.LocalUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器
 *
 * @author Nextera
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户信息管理相关接口")
@Validated
public class UserController {

    private final LocalUserService localUserService;

    private final AuthServiceClient authServiceClient;

    @GetMapping("/{userId}")
    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户详细信息")
    public Result<UserInfoDTO> getUserInfo(@Parameter(description = "用户ID") @PathVariable Long userId) {
        return Result.success(localUserService.getUserInfo(userId));
    }


    @DeleteMapping("/{userId}")
    @Operation(summary = "删除用户", description = "软删除用户")
    public Result<Integer> deleteUser(@Parameter(description = "用户ID") @PathVariable Long userId) {
        return Result.success(localUserService.deleteUser(userId));
    }

    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "用户服务健康检查接口")
    public Result<String> health() {
        return Result.success("User Service is running");
    }

    /**
     * 测试认证服务连接
     */
    @GetMapping("/test-auth")
    @Operation(summary = "测试认证服务连接", description = "测试OpenFeign调用认证服务")
    public Result<String> testAuthService() {
        try {
            Result<String> captchaResult = authServiceClient.getCaptcha();
            if (captchaResult.isSuccess()) {
                return Result.success("认证服务连接正常");
            } else {
                return Result.error("认证服务连接失败: " + captchaResult.getMessage());
            }
        } catch (Exception e) {
            return Result.error("认证服务连接异常: " + e.getMessage());
        }
    }
} 