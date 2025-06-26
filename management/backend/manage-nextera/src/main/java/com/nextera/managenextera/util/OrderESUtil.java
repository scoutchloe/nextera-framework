package com.nextera.managenextera.util;

import com.nextera.managenextera.dto.OrderManageDTO;
import com.nextera.managenextera.dto.OrderSearchDTO;
import com.nextera.managenextera.entity.OrderES;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;

import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单ES工具类
 *
 * @author nextera
 * @since 2025-01-01
 */
public class OrderESUtil {


    /**
     * 构建ES查询条件
     */
    public static Query buildSearchQuery(OrderSearchDTO searchDTO) {
        Criteria criteria = new Criteria();

        // 订单号模糊查询
        if (StringUtils.hasText(searchDTO.getOrderNo())) {
            criteria = criteria.and("orderNo").contains(searchDTO.getOrderNo());
        }

        // 用户名模糊查询
        if (StringUtils.hasText(searchDTO.getUsername())) {
            criteria = criteria.and("userName").contains(searchDTO.getUsername());
        }

        // 订单状态查询（处理状态列表）
        if (searchDTO.getStatusList() != null && !searchDTO.getStatusList().isEmpty()) {
            criteria = criteria.and("status").in(searchDTO.getStatusList());
        }

        // 支付方式查询（处理支付方式列表）
        if (searchDTO.getPaymentMethodList() != null && !searchDTO.getPaymentMethodList().isEmpty()) {
            criteria = criteria.and("paymentMethod").in(searchDTO.getPaymentMethodList());
        }

        // 金额范围查询
        if (searchDTO.getMinAmount() != null && searchDTO.getMaxAmount() != null) {
            criteria = criteria.and("totalAmount").between(searchDTO.getMinAmount(), searchDTO.getMaxAmount());
        } else if (searchDTO.getMinAmount() != null) {
            criteria = criteria.and("totalAmount").greaterThanEqual(searchDTO.getMinAmount());
        } else if (searchDTO.getMaxAmount() != null) {
            criteria = criteria.and("totalAmount").lessThanEqual(searchDTO.getMaxAmount());
        }

        // 时间范围查询
        if (searchDTO.getCreatedTimeStart() != null && searchDTO.getCreatedTimeEnd() != null) {
            criteria = criteria.and("createdTime").between(searchDTO.getCreatedTimeStart(), searchDTO.getCreatedTimeEnd());
        } else if (searchDTO.getCreatedTimeStart() != null) {
            criteria = criteria.and("createdTime").greaterThanEqual(searchDTO.getCreatedTimeStart());
        } else if (searchDTO.getCreatedTimeEnd() != null) {
            criteria = criteria.and("createdTime").lessThanEqual(searchDTO.getCreatedTimeEnd());
        }

        // 构建分页
        Pageable pageable = buildPageable(searchDTO);

        // 创建基础查询
        CriteriaQuery query = new CriteriaQuery(criteria).setPageable(pageable);
        
        // 如果有商品名称查询，我们需要特殊处理
        // 暂时先不加入criteria中，而是在Service层进行后处理
        return query;
    }

    /**
     * 构建分页对象
     */
    public static Pageable buildPageable(OrderSearchDTO searchDTO) {
        // 设置默认值
        int page = searchDTO.getCurrent() != null ? searchDTO.getCurrent().intValue() - 1 : 0;
        int size = searchDTO.getSize() != null ? searchDTO.getSize().intValue() : 10;

        // 构建排序
        Sort sort = Sort.by(Sort.Direction.DESC, "createdTime");
        if (StringUtils.hasText(searchDTO.getSortField())) {
            Sort.Direction direction = "asc".equalsIgnoreCase(searchDTO.getSortOrder()) 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(direction, searchDTO.getSortField());
        }

        return PageRequest.of(page, size, sort);
    }

    /**
     * 将OrderES转换为OrderManageDTO
     */
    public static OrderManageDTO convertToOrderManageDTO(OrderES orderES) {
        if (orderES == null) {
            return null;
        }

        OrderManageDTO dto = new OrderManageDTO();
        dto.setId(orderES.getId());
        dto.setOrderNo(orderES.getOrderNo());
        dto.setUserId(orderES.getUserId());
        dto.setUsername(orderES.getUserName());
        dto.setTotalAmount(orderES.getTotalAmount());
        dto.setStatus(orderES.getStatus());
        dto.setStatusDescription(getStatusName(orderES.getStatus()));
        dto.setPaymentMethod(orderES.getPaymentMethod());
        dto.setPaymentMethodDescription(getPayMethodName(orderES.getPaymentMethod()));
        dto.setRemark(orderES.getRemark());
        dto.setCreatedTime(orderES.getCreatedTime());
        dto.setUpdatedTime(orderES.getUpdatedTime());

        // 转换订单明细
        if (orderES.getOrderItems() != null) {
            List<OrderManageDTO.OrderItemDTO> itemDTOs = orderES.getOrderItems().stream()
                    .map(OrderESUtil::convertToOrderItemDTO)
                    .collect(Collectors.toList());
            dto.setOrderItems(itemDTOs);
        }

        return dto;
    }

    /**
     * 将OrderItemES转换为OrderItemDTO
     */
    public static OrderManageDTO.OrderItemDTO convertToOrderItemDTO(OrderES.OrderItemES itemES) {
        if (itemES == null) {
            return null;
        }

        OrderManageDTO.OrderItemDTO dto = new OrderManageDTO.OrderItemDTO();
        dto.setId(itemES.getId());
        dto.setOrderId(itemES.getOrderId());
        dto.setProductId(itemES.getProductId());
        dto.setProductName(itemES.getProductName());
        dto.setProductPrice(itemES.getProductPrice());
        dto.setQuantity(itemES.getQuantity());
        dto.setTotalPrice(itemES.getTotalPrice());
        dto.setCreatedTime(itemES.getCreatedTime());

        return dto;
    }

    /**
     * 批量转换OrderES列表为OrderManageDTO列表
     */
    public static List<OrderManageDTO> convertToOrderManageDTOList(List<OrderES> orderESList) {
        if (orderESList == null || orderESList.isEmpty()) {
            return new ArrayList<>();
        }

        return orderESList.stream()
                .map(OrderESUtil::convertToOrderManageDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取订单状态名称
     */
    public static String getStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "待支付";
            case 1 -> "已支付";
            case 2 -> "已发货";
            case 3 -> "已完成";
            case 4 -> "已取消";
            case 5 -> "已退款";
            default -> "未知";
        };
    }

    /**
     * 获取支付方式名称
     */
    public static String getPayMethodName(Integer paymentMethod) {
        if (paymentMethod == null) {
            return "未知";
        }
        return switch (paymentMethod) {
            case 1 -> "微信支付";
            case 2 -> "支付宝";
            case 3 -> "银行卡";
            case 4 -> "现金";
            default -> "其他";
        };
    }

    /**
     * 获取支付状态名称
     */
    public static String getPayStatusName(Integer payStatus) {
        if (payStatus == null) {
            return "未知";
        }
        return switch (payStatus) {
            case 0 -> "未支付";
            case 1 -> "已支付";
            case 2 -> "支付失败";
            case 3 -> "已退款";
            default -> "未知";
        };
    }

    /**
     * 获取配送状态名称
     */
    public static String getDeliveryStatusName(Integer deliveryStatus) {
        if (deliveryStatus == null) {
            return "未知";
        }
        return switch (deliveryStatus) {
            case 0 -> "未发货";
            case 1 -> "已发货";
            case 2 -> "运输中";
            case 3 -> "已送达";
            case 4 -> "配送失败";
            default -> "未知";
        };
    }
} 