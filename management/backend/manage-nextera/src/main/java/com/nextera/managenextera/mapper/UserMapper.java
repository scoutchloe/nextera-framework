package com.nextera.managenextera.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nextera.managenextera.entity.usermod.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
@DS("user")
public interface UserMapper extends BaseMapper<User> {
} 