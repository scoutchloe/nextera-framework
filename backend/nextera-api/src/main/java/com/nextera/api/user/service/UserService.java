package com.nextera.api.user.service;

import com.nextera.api.user.dto.UserDTO;
import com.nextera.common.core.Result;

/**
 * 用户服务Dubbo接口
 *
 * @author nextera
 * @since 2025-06-16
 */
public interface UserService {

    /**
     * 根据ID获取用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    Result<UserDTO> getUserById(Long id);

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    Result<UserDTO> getUserByUsername(String username);

    /**
     * 根据邮箱获取用户信息
     *
     * @param email 邮箱
     * @return 用户信息
     */
    Result<UserDTO> getUserByEmail(String email);

    /**
     * 验证用户是否存在
     *
     * @param userId 用户ID
     * @return 是否存在
     */
    Result<Boolean> existsUser(Long userId);

} 