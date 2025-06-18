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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional(rollbackFor = Exception.class)
    public boolean updateLastLoginTime(Long userId) {
        try {
            LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(User::getId, userId);
            updateWrapper.set(User::getLastLoginTime, LocalDateTime.now());
            boolean result = new LambdaUpdateChainWrapper<>(userMapper, updateWrapper).update();
            log.debug("更新用户最后活动时间，用户ID: {}, 结果: {}", userId, result);
            return result;
        } catch (Exception e) {
            log.error("更新用户最后活动时间失败，用户ID: {}", userId, e);
            throw e; // 重新抛出异常，确保本地事务回滚
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean restoreLastLoginTime(Long userId, LocalDateTime lastLoginTime) {
        try {
            LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(User::getId, userId);
            updateWrapper.set(User::getLastLoginTime, lastLoginTime);
            boolean result = new LambdaUpdateChainWrapper<>(userMapper, updateWrapper).update();
            log.info("TCC补偿 - 恢复用户最后活动时间，用户ID: {}, 恢复时间: {}, 结果: {}", 
                    userId, lastLoginTime, result);
            return result;
        } catch (Exception e) {
            log.error("TCC补偿 - 恢复用户最后活动时间失败，用户ID: {}", userId, e);
            throw e;
        }
    }


    @Override
    public int deleteUser(Long userId) {
        return userMapper.deleteById(userId);
    }
}