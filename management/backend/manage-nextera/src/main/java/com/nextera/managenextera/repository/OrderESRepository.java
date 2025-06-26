package com.nextera.managenextera.repository;

import com.nextera.managenextera.entity.OrderES;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 订单ES Repository
 *
 * @author nextera
 * @since 2025-01-01
 */
@Repository
public interface OrderESRepository extends ElasticsearchRepository<OrderES, String> {

    /**
     * 根据订单ID查询
     */
    Optional<OrderES> findById(Long id);

    /**
     * 根据订单号查询
     */
    Optional<OrderES> findByOrderNo(String orderNo);

    /**
     * 根据用户ID查询订单列表
     */
    Page<OrderES> findByUserId(Long userId, Pageable pageable);

    /**
     * 根据订单状态查询
     */
    Page<OrderES> findByStatus(Integer status, Pageable pageable);

    /**
     * 根据订单状态列表查询
     */
    Page<OrderES> findByStatusIn(List<Integer> statusList, Pageable pageable);

    /**
     * 根据支付状态查询
     */
    Page<OrderES> findByPayStatus(Integer payStatus, Pageable pageable);

    /**
     * 根据时间范围查询
     */
    Page<OrderES> findByCreatedTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 根据用户名模糊查询
     */
    @Query("{\"bool\":{\"must\":[{\"wildcard\":{\"userName\":\"*?0*\"}}]}}")
    Page<OrderES> findByUserNameContaining(String userName, Pageable pageable);

    /**
     * 根据订单号模糊查询
     */
    @Query("{\"bool\":{\"must\":[{\"wildcard\":{\"orderNo\":\"*?0*\"}}]}}")
    Page<OrderES> findByOrderNoContaining(String orderNo, Pageable pageable);

    /**
     * 根据商品名称模糊查询（在嵌套字段中搜索）
     */
    @Query("{\"bool\":{\"must\":[{\"nested\":{\"path\":\"items\",\"query\":{\"bool\":{\"must\":[{\"wildcard\":{\"items.productName\":\"*?0*\"}}]}}}}]}}")
    Page<OrderES> findByProductNameContaining(String productName, Pageable pageable);

    /**
     * 金额范围查询
     */
    Page<OrderES> findByTotalAmountBetween(BigDecimal minAmount, BigDecimal maxAmount, Pageable pageable);

    /**
     * 复合查询 - 按订单号、用户名、状态、时间范围等条件
     */
    @Query("{\"bool\":{\"must\":[" +
            "{\"bool\":{\"should\":[" +
            "{\"wildcard\":{\"orderNo\":\"*?0*\"}}," +
            "{\"wildcard\":{\"userName\":\"*?0*\"}}," +
            "{\"nested\":{\"path\":\"items\",\"query\":{\"bool\":{\"must\":[{\"wildcard\":{\"items.productName\":\"*?0*\"}}]}}}}" +
            "]}}," +
            "?1" +
            "]}}")
    Page<OrderES> searchOrders(String keyword, String additionalQuery, Pageable pageable);

    /**
     * 统计订单总数
     */
    long count();

    /**
     * 根据状态统计订单数量
     */
    long countByStatus(Integer status);

    /**
     * 根据时间范围统计订单数量
     */
    long countByCreatedTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据状态和时间范围统计订单数量
     */
    long countByStatusAndCreatedTimeBetween(Integer status, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据时间范围查询订单列表（不分页）
     */
    List<OrderES> findByCreatedTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据状态和时间范围查询订单列表
     */
    List<OrderES> findByStatusAndCreatedTimeBetween(Integer status, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据状态列表和时间范围查询订单列表
     */
    List<OrderES> findByStatusInAndCreatedTimeBetween(List<Integer> statusList, LocalDateTime startTime, LocalDateTime endTime);
} 