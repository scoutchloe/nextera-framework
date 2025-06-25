package com.nextera.id.generator;

import com.nextera.idapi.dto.IdSegment;
import com.nextera.idapi.dto.IdStatus;

import java.util.List;

/**
 * ID生成器接口
 * 定义ID生成器的基本功能
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
public interface IdGenerator {
    
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
     *
     * @param businessType 业务类型
     * @param segmentSize 段大小
     * @return ID段对象
     */
    IdSegment generateIdSegment(String businessType, int segmentSize);
    
    /**
     * 重置ID计数器
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
     *
     * @param businessType 业务类型
     * @param startValue 起始值
     */
    void initBusinessType(String businessType, Long startValue);
    
    /**
     * 删除业务类型
     *
     * @param businessType 业务类型
     */
    void removeBusinessType(String businessType);
    
    /**
     * 检查生成器是否支持指定业务类型
     *
     * @param businessType 业务类型
     * @return 是否支持
     */
    boolean supports(String businessType);
} 