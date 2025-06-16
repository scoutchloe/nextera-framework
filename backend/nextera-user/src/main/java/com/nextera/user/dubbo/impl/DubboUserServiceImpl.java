package com.nextera.user.dubbo.impl;

import cn.hutool.core.bean.BeanUtil;
import com.nextera.api.user.dto.UserDTO;
import com.nextera.api.user.service.UserService;
import com.nextera.common.core.Result;
import com.nextera.user.dto.UserInfoDTO;
import com.nextera.user.entity.User;
import com.nextera.user.service.LocalUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

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
public class DubboUserServiceImpl implements UserService {

    private final LocalUserService localUserservice;

    @Override
    public Result<UserDTO> getUserById(Long id) {
        try {
            UserInfoDTO user = localUserservice.getUserInfo(id);
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
        return null;
    }

    @Override
    public Result<UserDTO> getUserByEmail(String email) {
        return null;
    }

    @Override
    public Result<Boolean> existsUser(Long userId) {
        return null;
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

    private UserDTO convertToDTO(UserInfoDTO user) {
        UserDTO dto = new UserDTO();
        BeanUtil.copyProperties(user, dto);
        // 不返回敏感信息如密码
        return dto;
    }
} 