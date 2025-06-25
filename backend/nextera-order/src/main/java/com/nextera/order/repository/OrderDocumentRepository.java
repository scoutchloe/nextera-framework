package com.nextera.order.repository;

import com.nextera.order.document.OrderDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单文档Repository
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
@Repository
public interface OrderDocumentRepository extends ElasticsearchRepository<OrderDocument, Long> {

    /**
     * 根据用户ID查询订单
     *
     * @param userId 用户ID
     * @return 订单列表
     */
    List<OrderDocument> findByUserId(Long userId);

    /**
     * 根据订单号查询订单
     *
     * @param orderNo 订单号
     * @return 订单
     */
    OrderDocument findByOrderNo(String orderNo);

    /**
     * 根据订单状态查询订单
     *
     * @param status 订单状态
     * @param pageable 分页参数
     * @return 订单分页列表
     */
    Page<OrderDocument> findByStatus(Integer status, Pageable pageable);

    /**
     * 根据用户ID和状态查询订单
     *
     * @param userId 用户ID
     * @param status 订单状态
     * @return 订单列表
     */
    List<OrderDocument> findByUserIdAndStatus(Long userId, Integer status);

    /**
     * 根据创建时间范围查询订单
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 订单分页列表
     */
    Page<OrderDocument> findByCreatedTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 根据创建时间范围查询所有订单（不分页）
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 订单列表
     */
    List<OrderDocument> findAllByCreatedTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据用户ID和创建时间范围查询订单
     *
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 订单列表
     */
    List<OrderDocument> findByUserIdAndCreatedTimeBetween(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据备注关键字搜索订单
     *
     * @param keyword 关键字
     * @param pageable 分页参数
     * @return 订单分页列表
     */
    Page<OrderDocument> findByRemarkContaining(String keyword, Pageable pageable);

    /**
     * 根据商品名称搜索订单
     *
     * @param productName 商品名称
     * @param pageable 分页参数
     * @return 订单分页列表
     */
    Page<OrderDocument> findByOrderItemsProductNameContaining(String productName, Pageable pageable);

    /**
     * 统计用户订单数量
     *
     * @param userId 用户ID
     * @return 订单数量
     */
    long countByUserId(Long userId);

    /**
     * 统计指定状态的订单数量
     *
     * @param status 订单状态
     * @return 订单数量
     */
    long countByStatus(Integer status);

    /**
     * 根据金额范围查询
     */
    List<OrderDocument> findByTotalAmountBetween(BigDecimal minAmount, BigDecimal maxAmount);

    /**
     * 根据状态和时间范围查询
     */
    Page<OrderDocument> findByStatusAndCreatedTimeBetween(Integer status, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    // 复杂聚合查询方法暂时注释掉，避免启动时的问题
    // 可以在ElasticSearch正常连接后再启用
    
    // Page<OrderDocument> aggregateOrdersByDate(Pageable pageable);
    // Page<OrderDocument> aggregateOrdersByStatus(Pageable pageable);  
    // Page<OrderDocument> aggregateProductSales(Pageable pageable);
} 