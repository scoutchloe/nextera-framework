package com.nextera.managenextera.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nextera.managenextera.entity.OperationLogs;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志Mapper接口
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLogs> {
} 