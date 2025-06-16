package com.nextera.user.client;

import com.nextera.api.user.dto.UserDTO;
import com.nextera.api.user.service.UserService;
import com.nextera.common.core.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/**
 * 用户服务客户端
 * 通过Dubbo调用用户服务
 *
 * @author nextera
 * @since 2025-06-16
 */
@Slf4j
@Component
public class UserServiceClient {

    @DubboReference(version = "1.0.0", timeout = 5000, retries = 0)
    private UserService userService;

    /**
     * 根据ID获取用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    public Result<UserDTO> getUserById(Long id) {
        try {
            log.debug("调用用户服务获取用户信息，用户ID: {}", id);
            return userService.getUserById(id);
        } catch (Exception e) {
            log.error("调用用户服务获取用户信息失败", e);
            return Result.error("获取用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    public Result<UserDTO> getUserByUsername(String username) {
        try {
            log.debug("调用用户服务根据用户名获取用户信息，用户名: {}", username);
            return userService.getUserByUsername(username);
        } catch (Exception e) {
            log.error("调用用户服务根据用户名获取用户信息失败", e);
            return Result.error("获取用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 根据邮箱获取用户信息
     *
     * @param email 邮箱
     * @return 用户信息
     */
    public Result<UserDTO> getUserByEmail(String email) {
        try {
            log.debug("调用用户服务根据邮箱获取用户信息，邮箱: {}", email);
            return userService.getUserByEmail(email);
        } catch (Exception e) {
            log.error("调用用户服务根据邮箱获取用户信息失败", e);
            return Result.error("获取用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 验证用户是否存在
     *
     * @param userId 用户ID
     * @return 是否存在
     */
    public Result<Boolean> existsUser(Long userId) {
        try {
            log.debug("调用用户服务验证用户是否存在，用户ID: {}", userId);
            return userService.existsUser(userId);
        } catch (Exception e) {
            log.error("调用用户服务验证用户是否存在失败", e);
            return Result.error("验证用户失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户最后登录时间
     *
     * @param userId 用户ID
     * @return 更新结果
     */
    public Result<Boolean> updateLastLoginTime(Long userId) {
        try {
            log.info("调用用户服务更新用户最后登录时间，用户ID: {}", userId);
            return userService.updateLastLoginTime(userId);
        } catch (Exception e) {
            log.error("调用用户服务更新用户最后登录时间失败", e);
            return Result.error("更新登录时间失败：" + e.getMessage());
        }
    }

    /**
     * 检查用户权限
     *
     * @param userId 用户ID
     * @param permission 权限代码
     * @return 是否有权限
     */
    public Result<Boolean> hasPermission(Long userId, String permission) {
        try {
            log.debug("调用用户服务检查用户权限，用户ID: {}, 权限: {}", userId, permission);
            return userService.hasPermission(userId, permission);
        } catch (Exception e) {
            log.error("调用用户服务检查用户权限失败", e);
            return Result.error("检查权限失败：" + e.getMessage());
        }
    }
} 