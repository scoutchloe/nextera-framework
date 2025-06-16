package com.nextera.user.dubbo.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nextera.api.user.dto.UserDTO;
import com.nextera.api.user.service.UserService;
import com.nextera.common.core.Result;
import com.nextera.user.entity.User;
import com.nextera.user.mapper.UserMapper;
import cn.hutool.core.bean.BeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户服务Dubbo实现类
 *
 * @author nextera
 * @since 2025-06-16
 */
@Slf4j
@Service
@DubboService(version = "1.0.0")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    public Result<UserDTO> getUserById(Long id) {
        try {
            User user = userMapper.selectById(id);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            UserDTO userDTO = convertToDTO(user);
            return Result.success(userDTO);
            
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.error("获取用户信息失败：" + e.getMessage());
        }
    }

    @Override
    public Result<UserDTO> getUserByUsername(String username) {
        try {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getUsername, username);
            
            User user = userMapper.selectOne(queryWrapper);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            UserDTO userDTO = convertToDTO(user);
            return Result.success(userDTO);
            
        } catch (Exception e) {
            log.error("根据用户名获取用户信息失败", e);
            return Result.error("获取用户信息失败：" + e.getMessage());
        }
    }

    @Override
    public Result<UserDTO> getUserByEmail(String email) {
        try {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getEmail, email);
            
            User user = userMapper.selectOne(queryWrapper);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            UserDTO userDTO = convertToDTO(user);
            return Result.success(userDTO);
            
        } catch (Exception e) {
            log.error("根据邮箱获取用户信息失败", e);
            return Result.error("获取用户信息失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Boolean> existsUser(Long userId) {
        try {
            User user = userMapper.selectById(userId);
            return Result.success(user != null);
            
        } catch (Exception e) {
            log.error("检查用户是否存在失败", e);
            return Result.error("检查用户失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Boolean> updateLastLoginTime(Long userId) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            user.setLastLoginTime(LocalDateTime.now());
            int result = userMapper.updateById(user);
            
            return Result.success(result > 0);
            
        } catch (Exception e) {
            log.error("更新用户最后登录时间失败", e);
            return Result.error("更新登录时间失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Boolean> hasPermission(Long userId, String permission) {
        try {
            // 这里可以根据实际的权限系统实现
            // 暂时简单实现：检查用户是否存在且状态正常
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.success(false);
            }
            
            // 检查用户状态是否正常（0-正常，1-禁用）
            boolean hasPermission = user.getStatus() == 0;
            
            return Result.success(hasPermission);
            
        } catch (Exception e) {
            log.error("检查用户权限失败", e);
            return Result.error("检查权限失败：" + e.getMessage());
        }
    }

    /**
     * 转换为DTO
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        BeanUtil.copyProperties(user, dto);
        // 不返回敏感信息如密码
        return dto;
    }
} 