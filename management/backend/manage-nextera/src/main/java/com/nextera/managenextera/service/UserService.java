package com.nextera.managenextera.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nextera.managenextera.dto.UserDTO;
import com.nextera.managenextera.entity.usermod.User;

import java.util.List;

/**
 * 用户服务接口
 *
 * @author nextera
 * @since 2025-06-19
 */
public interface UserService extends IService<User> {

    /**
     * 分页查询用户列表
     *
     * @param page 分页参数
     * @param username 用户名（可选）
     * @param nickname 昵称（可选）
     * @param email 邮箱（可选）
     * @param status 状态（可选）
     * @return 分页结果
     */
    IPage<UserDTO> getUserPage(Page<User> page, String username, String nickname, 
                               String email, Integer status);

    /**
     * 根据ID获取用户详情
     *
     * @param id 用户ID
     * @return 用户DTO
     */
    UserDTO getUserById(Long id);

    /**
     * 创建用户
     *
     * @param userDTO 用户DTO
     * @return 是否成功
     */
    boolean createUser(UserDTO userDTO);

    /**
     * 更新用户
     *
     * @param userDTO 用户DTO
     * @return 是否成功
     */
    boolean updateUser(UserDTO userDTO);

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 是否成功
     */
    boolean deleteUser(Long id);

    /**
     * 批量删除用户
     *
     * @param ids 用户ID列表
     * @return 是否成功
     */
    boolean deleteUsers(List<Long> ids);

    /**
     * 更新用户状态
     *
     * @param id 用户ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateUserStatus(Long id, Integer status);

    /**
     * 重置用户密码
     *
     * @param id 用户ID
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean resetPassword(Long id, String newPassword);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @param excludeId 排除的用户ID（用于更新时检查）
     * @return 是否存在
     */
    boolean isUsernameExists(String username, Long excludeId);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @param excludeId 排除的用户ID（用于更新时检查）
     * @return 是否存在
     */
    boolean isEmailExists(String email, Long excludeId);
} 