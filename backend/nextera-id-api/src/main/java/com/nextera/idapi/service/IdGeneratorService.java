package com.nextera.idapi.service;

import com.nextera.idapi.dto.IdSegment;
import com.nextera.idapi.dto.IdStatus;

import java.util.List;

/**
 * ID生成服务接口
 * 提供分布式ID生成相关功能
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
public interface IdGeneratorService {

    /**
     * 生成单个ID
     *
     * @param businessType 业务类型
     * @return 生成的ID
     */
    Long generateId(String businessType);

    /**
     * 批量生成ID
     *
     * @param businessType 业务类型
     * @param count 生成数量
     * @return ID列表
     */
    List<Long> generateIds(String businessType, int count);

    /**
     * 生成ID段
     * 返回一个ID段，调用方可以在这个段内自行分配ID
     *
     * @param businessType 业务类型
     * @param segmentSize 段大小
     * @return ID段对象
     */
    IdSegment generateIdSegment(String businessType, int segmentSize);

    /**
     * 重置ID计数器
     * 将指定业务类型的ID计数器重置为指定值
     *
     * @param businessType 业务类型
     * @param startValue 起始值
     */
    void resetIdCounter(String businessType, Long startValue);

    /**
     * 获取当前ID状态
     *
     * @param businessType 业务类型
     * @return ID状态信息
     */
    IdStatus getIdStatus(String businessType);

    /**
     * 获取所有业务类型的ID状态
     *
     * @return ID状态列表
     */
    List<IdStatus> getAllIdStatus();
    
    /**
     * 初始化业务类型
     * 为新的业务类型初始化ID生成器
     *
     * @param businessType 业务类型
     * @param startValue 起始值（可选，默认为1）
     */
    void initBusinessType(String businessType, Long startValue);
    
    /**
     * 删除业务类型
     * 删除指定业务类型的ID生成器配置
     *
     * @param businessType 业务类型
     */
    void removeBusinessType(String businessType);
} 