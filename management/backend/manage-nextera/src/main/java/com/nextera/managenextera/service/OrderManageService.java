package com.nextera.managenextera.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nextera.managenextera.dto.OrderManageDTO;
import com.nextera.managenextera.dto.OrderSearchDTO;

import java.util.List;

/**
 * 管理端订单业务逻辑服务接口
 *
 * @author nextera
 * @since 2025-01-01
 */
public interface OrderManageService {

    /**
     * 分页查询订单（基于ES）
     *
     * @param searchDTO 搜索条件
     * @return 分页结果
     */
    IPage<OrderManageDTO> getOrderPage(OrderSearchDTO searchDTO);

    /**
     * 根据ID查询订单详情
     *
     * @param orderId 订单ID
     * @return 订单详情
     */
    OrderManageDTO getOrderDetail(Long orderId);

    /**
     * 根据订单号查询订单详情
     *
     * @param orderNo 订单号
     * @return 订单详情
     */
    OrderManageDTO getOrderDetailByOrderNo(String orderNo);

    /**
     * 更新订单状态
     *
     * @param orderId 订单ID
     * @param status 新状态
     * @return 是否成功
     */
    boolean updateOrderStatus(Long orderId, Integer status);

    /**
     * 批量更新订单状态
     *
     * @param orderIds 订单ID列表
     * @param status 新状态
     * @return 是否成功
     */
    boolean batchUpdateOrderStatus(List<Long> orderIds, Integer status);

    /**
     * 高级搜索订单
     *
     * @param searchDTO 搜索条件
     * @return 搜索结果
     */
    List<OrderManageDTO> advancedSearch(OrderSearchDTO searchDTO);

    /**
     * 导出订单数据
     *
     * @param searchDTO 搜索条件
     * @return 导出数据
     */
    List<OrderManageDTO> exportOrders(OrderSearchDTO searchDTO);

    /**
     * 获取订单状态分布
     *
     * @return 状态分布
     */
    List<OrderManageDTO> getOrderStatusDistribution();
} 