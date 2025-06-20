package com.nextera.managenextera.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextera.managenextera.common.Result;
import com.nextera.managenextera.dto.UserDTO;
import com.nextera.managenextera.entity.usermod.User;
import com.nextera.managenextera.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户管理Controller
 *
 * @author nextera
 * @since 2025-06-19
 */
@Tag(name = "用户管理", description = "用户管理相关接口")
@RestController
@RequestMapping("/system/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "分页查询用户", description = "分页查询用户列表")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('user:list')")
    public Result<IPage<UserDTO>> getUserPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Integer status) {

        Page<User> page = new Page<>(current, size);
        IPage<UserDTO> result = userService.getUserPage(page, username, nickname, email, status);

        return Result.success(result);
    }

    @Operation(summary = "根据ID获取用户详情", description = "根据用户ID获取用户详细信息")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:list')")
    public Result<UserDTO> getUserById(
            @Parameter(description = "用户ID") @PathVariable Long id) {

        UserDTO user = userService.getUserById(id);
        if (user != null) {
            return Result.success(user);
        } else {
            return Result.error("用户不存在");
        }
    }

    @Operation(summary = "创建用户", description = "创建新用户")
    @PostMapping
    @PreAuthorize("hasAuthority('user:add')")
    public Result<Boolean> createUser(@Validated @RequestBody UserDTO userDTO) {
        boolean success = userService.createUser(userDTO);
        return Result.success(success);
    }

    @Operation(summary = "更新用户", description = "更新用户信息")
    @PutMapping
    @PreAuthorize("hasAuthority('user:edit')")
    public Result<Boolean> updateUser(@Validated @RequestBody UserDTO userDTO) {
        boolean success = userService.updateUser(userDTO);
        return Result.success(success);
    }

    @Operation(summary = "删除用户", description = "根据ID删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public Result<Boolean> deleteUser(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        boolean success = userService.deleteUser(id);
        return Result.success(success);
    }

    @Operation(summary = "批量删除用户", description = "批量删除用户")
    @DeleteMapping("/batch")
    @PreAuthorize("hasAuthority('user:delete')")
    public Result<Boolean> deleteUsers(@RequestBody List<Long> ids) {
        boolean success = userService.deleteUsers(ids);
        return Result.success(success);
    }

    @Operation(summary = "更新用户状态", description = "更新用户状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('user:edit')")
    public Result<Boolean> updateUserStatus(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @RequestParam Integer status) {
        boolean success = userService.updateUserStatus(id, status);
        return Result.success(success);
    }

    @Operation(summary = "重置用户密码", description = "重置用户密码")
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('user:edit')")
    public Result<Boolean> resetPassword(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @RequestParam(defaultValue = "123456") String newPassword) {
        boolean success = userService.resetPassword(id, newPassword);
        return Result.success(success);
    }

    @Operation(summary = "检查用户名是否存在", description = "检查用户名是否已存在")
    @GetMapping("/check-username")
    @PreAuthorize("hasAuthority('user:list')")
    public Result<Boolean> checkUsername(
            @RequestParam String username,
            @RequestParam(required = false) Long excludeId) {
        boolean exists = userService.isUsernameExists(username, excludeId);
        return Result.success(exists);
    }

    @Operation(summary = "检查邮箱是否存在", description = "检查邮箱是否已存在")
    @GetMapping("/check-email")
    @PreAuthorize("hasAuthority('user:list')")
    public Result<Boolean> checkEmail(
            @RequestParam String email,
            @RequestParam(required = false) Long excludeId) {
        boolean exists = userService.isEmailExists(email, excludeId);
        return Result.success(exists);
    }
} 