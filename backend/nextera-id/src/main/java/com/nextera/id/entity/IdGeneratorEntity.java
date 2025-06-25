package com.nextera.id.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ID生成器实体类
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
@Data
@TableName("id_generator")
public class IdGeneratorEntity {
    
    /**
     * 业务类型（主键）
     */
    @TableId(type = IdType.INPUT)
    private String businessType;
    
    /**
     * 当前最大ID
     */
    private Long maxId;
    
    /**
     * 步长大小
     */
    private Integer stepSize;
    
    /**
     * 总生成数量
     */
    private Long totalGenerated;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 备注
     */
    private String remark;
} 