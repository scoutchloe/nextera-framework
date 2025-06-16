package com.nextera.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nextera.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 *
 * @author nextera
 * @since 2025-06-16
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

} 