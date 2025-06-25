package com.nextera.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nextera.order.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单明细Mapper接口
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /**
     * 根据订单ID查询订单明细列表
     *
     * @param orderId 订单ID
     * @return 订单明细列表
     */
    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 批量插入订单明细
     *
     * @param orderItems 订单明细列表
     * @return 插入数量
     */
    int batchInsert(@Param("list") List<OrderItem> orderItems);

    /**
     * 根据订单ID删除订单明细
     *
     * @param orderId 订单ID
     * @return 删除数量
     */
    int deleteByOrderId(@Param("orderId") Long orderId);
} 