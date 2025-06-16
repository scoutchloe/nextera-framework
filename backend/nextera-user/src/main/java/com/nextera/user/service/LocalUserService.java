package com.nextera.user.service;

import com.nextera.user.dto.UserInfoDTO;

/**
 * 用户服务接口
 *
 * @author Nextera
 */
public interface LocalUserService {

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserInfoDTO getUserInfo(Long userId);

    /**
     * 根据用户ID 更新最后登录时间
     * @param userId
     * @return
     */
    boolean updateLastLoginTime(Long userId);


    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return 删除结果
     */
    int deleteUser(Long userId);
} 