package com.nextera.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nextera.user.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * 用户资料Mapper接口
 *
 * @author Nextera
 */
@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {

    /**
     * 根据用户ID查询用户资料
     *
     * @param userId 用户ID
     * @return 用户资料
     */
    @Select("SELECT * FROM user_profile WHERE user_id = #{userId}")
    UserProfile selectByUserId(Long userId);
} 