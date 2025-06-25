package com.nextera.idapi.dto;

import com.nextera.idapi.enums.IdGeneratorType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * ID状态信息
 * 用于展示当前ID生成器的状态
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdStatus implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 业务类型
     */
    private String businessType;
    
    /**
     * 当前最大ID
     */
    private Long currentMaxId;
    
    /**
     * ID生成器类型
     */
    private IdGeneratorType generatorType;
    
    /**
     * 批量获取大小
     */
    private Integer batchSize;
    
    /**
     * 总共生成的ID数量
     */
    private Long totalGenerated;
    
    /**
     * 是否可用
     */
    private Boolean available;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 最后更新时间
     */
    private Long updateTime;
    
    /**
     * 备注信息
     */
    private String remark;
} 