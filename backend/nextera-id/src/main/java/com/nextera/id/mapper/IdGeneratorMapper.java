package com.nextera.id.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nextera.id.entity.IdGeneratorEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * ID生成器Mapper接口
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
@Mapper
public interface IdGeneratorMapper extends BaseMapper<IdGeneratorEntity> {
    
    /**
     * 获取下一个ID段
     * 使用数据库行锁确保并发安全
     *
     * @param businessType 业务类型
     * @param stepSize 步长
     * @return 起始ID数组 [startId, endId]
     */
    @Select("SELECT get_next_id_batch(#{businessType}, #{stepSize})")
    Long[] getNextIdBatch(@Param("businessType") String businessType, 
                         @Param("stepSize") Integer stepSize);
    
    /**
     * 更新最大ID（使用乐观锁）
     *
     * @param businessType 业务类型
     * @param newMaxId 新的最大ID
     * @param oldMaxId 原来的最大ID
     * @param totalIncrement 总增量
     * @return 影响的行数
     */
    @Update("UPDATE id_generator SET max_id = #{newMaxId}, " +
            "total_generated = total_generated + #{totalIncrement}, " +
            "updated_at = CURRENT_TIMESTAMP " +
            "WHERE business_type = #{businessType} AND max_id = #{oldMaxId}")
    int updateMaxIdWithOptimisticLock(@Param("businessType") String businessType,
                                     @Param("newMaxId") Long newMaxId,
                                     @Param("oldMaxId") Long oldMaxId,
                                     @Param("totalIncrement") Long totalIncrement);
    
    /**
     * 重置ID计数器
     *
     * @param businessType 业务类型
     * @param startValue 起始值
     * @return 影响的行数
     */
    @Update("UPDATE id_generator SET max_id = #{startValue} - 1, " +
            "total_generated = 0, " +
            "updated_at = CURRENT_TIMESTAMP " +
            "WHERE business_type = #{businessType}")
    int resetIdCounter(@Param("businessType") String businessType,
                      @Param("startValue") Long startValue);
} 