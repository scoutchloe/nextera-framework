package com.nextera.managenextera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nextera.managenextera.dto.UserDTO;
import com.nextera.managenextera.entity.usermod.User;
import com.nextera.managenextera.mapper.UserMapper;
import com.nextera.managenextera.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务实现类
 *
 * @author nextera
 * @since 2025-06-19
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public IPage<UserDTO> getUserPage(Page<User> page, String username, String nickname, 
                                      String email, Integer status) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(username)) {
            queryWrapper.like(User::getUsername, username);
        }
        if (StringUtils.hasText(nickname)) {
            queryWrapper.like(User::getNickname, nickname);
        }
        if (StringUtils.hasText(email)) {
            queryWrapper.like(User::getEmail, email);
        }
        if (status != null) {
            queryWrapper.eq(User::getStatus, status);
        }

        queryWrapper.orderByDesc(User::getCreateTime);

        IPage<User> userPage = this.page(page, queryWrapper);
        
        // 转换为DTO
        IPage<UserDTO> result = userPage.convert(this::convertToDTO);
        
        return result;
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = this.getById(id);
        if (user == null) {
            return null;
        }
        return convertToDTO(user);
    }

    @Override
    @Transactional
    public boolean createUser(UserDTO userDTO) {
        // 检查用户名是否已存在
        if (isUsernameExists(userDTO.getUsername(), null)) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (StringUtils.hasText(userDTO.getEmail()) && isEmailExists(userDTO.getEmail(), null)) {
            throw new RuntimeException("邮箱已存在");
        }

        User user = convertToEntity(userDTO);
        
        // 设置默认密码并加密
        if (!StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode("123456")); // 默认密码
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setIsDeleted(0);
        
        return this.save(user);
    }

    @Override
    @Transactional
    public boolean updateUser(UserDTO userDTO) {
        User existingUser = this.getById(userDTO.getId());
        if (existingUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查用户名是否已存在（排除自己）
        if (!existingUser.getUsername().equals(userDTO.getUsername()) && 
            isUsernameExists(userDTO.getUsername(), userDTO.getId())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在（排除自己）
        if (StringUtils.hasText(userDTO.getEmail()) && 
            !userDTO.getEmail().equals(existingUser.getEmail()) && 
            isEmailExists(userDTO.getEmail(), userDTO.getId())) {
            throw new RuntimeException("邮箱已存在");
        }

        User user = convertToEntity(userDTO);
        user.setPassword(existingUser.getPassword()); // 保持原密码
        user.setUpdateTime(LocalDateTime.now());
        
        return this.updateById(user);
    }

    @Override
    @Transactional
    public boolean deleteUser(Long id) {
        return this.removeById(id);
    }

    @Override
    @Transactional
    public boolean deleteUsers(List<Long> ids) {
        return this.removeByIds(ids);
    }

    @Override
    @Transactional
    public boolean updateUserStatus(Long id, Integer status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        return this.updateById(user);
    }

    @Override
    @Transactional
    public boolean resetPassword(Long id, String newPassword) {
        User user = new User();
        user.setId(id);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        return this.updateById(user);
    }

    @Override
    public boolean isUsernameExists(String username, Long excludeId) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        
        if (excludeId != null) {
            queryWrapper.ne(User::getId, excludeId);
        }
        
        return this.count(queryWrapper) > 0;
    }

    @Override
    public boolean isEmailExists(String email, Long excludeId) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, email);
        
        if (excludeId != null) {
            queryWrapper.ne(User::getId, excludeId);
        }
        
        return this.count(queryWrapper) > 0;
    }

    /**
     * 实体转DTO
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        
        // 不返回密码
        dto.setStatusName(getStatusName(user.getStatus()));
        dto.setGenderName(getGenderName(user.getGender()));
        
        return dto;
    }

    /**
     * DTO转实体
     */
    private User convertToEntity(UserDTO dto) {
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        return user;
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "正常";
            case 1:
                return "禁用";
            default:
                return "未知";
        }
    }

    /**
     * 获取性别名称
     */
    private String getGenderName(Integer gender) {
        if (gender == null) {
            return "未知";
        }
        switch (gender) {
            case 0:
                return "未知";
            case 1:
                return "男";
            case 2:
                return "女";
            default:
                return "未知";
        }
    }
} 