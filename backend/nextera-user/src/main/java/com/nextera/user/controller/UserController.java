package com.nextera.user.controller;

import com.nextera.common.core.Result;
import com.nextera.user.dto.UserInfoDTO;
import com.nextera.user.service.UserService;
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

    private final UserService userService;

    @GetMapping("/{userId}")
    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户详细信息")
    public Result<UserInfoDTO> getUserInfo(@Parameter(description = "用户ID") @PathVariable Long userId) {
        return Result.success(userService.getUserInfo(userId));
    }


    @DeleteMapping("/{userId}")
    @Operation(summary = "删除用户", description = "软删除用户")
    public Result<Integer> deleteUser(@Parameter(description = "用户ID") @PathVariable Long userId) {
        return Result.success(userService.deleteUser(userId));
    }

    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "用户服务健康检查接口")
    public Result<String> health() {
        return Result.success("User Service is running");
    }
} 