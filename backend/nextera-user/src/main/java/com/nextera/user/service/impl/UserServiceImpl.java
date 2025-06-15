package com.nextera.user.service.impl;

import com.nextera.user.dto.UserInfoDTO;
import com.nextera.user.entity.UserProfile;
import com.nextera.user.mapper.UserProfileMapper;
import com.nextera.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 *
 * @author Nextera
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserProfileMapper userProfileMapper;

    @Override
    public UserInfoDTO getUserInfo(Long userId) {
        UserProfile userProfile = userProfileMapper.selectByUserId(userId);
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        BeanUtils.copyProperties(userProfile, userInfoDTO);
        return userInfoDTO;
    }


    @Override
    public int deleteUser(Long userId) {
        return userProfileMapper.deleteById(userId);
    }
}