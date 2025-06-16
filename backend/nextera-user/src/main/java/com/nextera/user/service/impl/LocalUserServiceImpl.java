package com.nextera.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.nextera.user.dto.UserInfoDTO;
import com.nextera.user.entity.User;
import com.nextera.user.mapper.UserMapper;
import com.nextera.user.service.LocalUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户服务实现类
 *
 * @author Nextera
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalUserServiceImpl implements LocalUserService {


    private final UserMapper userMapper;

    @Override
    public UserInfoDTO getUserInfo(Long userId) {
        User user = userMapper.selectByUserId(userId);
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        BeanUtils.copyProperties(user, userInfoDTO);
        return userInfoDTO;
    }

    @Override
    public boolean updateLastLoginTime(Long userId) {
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId);
        updateWrapper.set(User::getLastLoginTime, LocalDateTime.now());
        return new LambdaUpdateChainWrapper<>(userMapper, updateWrapper).update();
    }


    @Override
    public int deleteUser(Long userId) {
        return userMapper.deleteById(userId);
    }
}