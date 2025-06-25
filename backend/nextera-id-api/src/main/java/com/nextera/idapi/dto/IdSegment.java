package com.nextera.idapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * ID段对象
 * 用于批量获取ID时返回ID段信息
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdSegment implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 起始ID
     */
    private Long startId;
    
    /**
     * 结束ID
     */
    private Long endId;
    
    /**
     * 业务类型
     */
    private String businessType;
    
    /**
     * 段大小
     */
    private Integer segmentSize;
    
    /**
     * 创建时间戳
     */
    private Long timestamp;
    
    /**
     * 检查ID是否在当前段内
     *
     * @param id 要检查的ID
     * @return 是否在段内
     */
    public boolean contains(Long id) {
        return id != null && id >= startId && id <= endId;
    }
    
    /**
     * 获取段内剩余ID数量
     *
     * @param currentId 当前已使用的ID
     * @return 剩余数量
     */
    public long remainingCount(Long currentId) {
        if (currentId == null || currentId < startId) {
            return endId - startId + 1;
        }
        if (currentId > endId) {
            return 0;
        }
        return endId - currentId;
    }
}