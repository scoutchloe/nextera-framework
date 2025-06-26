package com.nextera.managenextera.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单导出DTO
 *
 * @author nextera
 * @since 2025-01-01
 */
@Data
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
public class OrderExportDTO {

    @ExcelProperty(value = "订单号", index = 0)
    @ColumnWidth(20)
    private String orderNo;

    @ExcelProperty(value = "用户名", index = 1)
    @ColumnWidth(15)
    private String username;

    @ExcelProperty(value = "订单金额", index = 2)
    @ColumnWidth(12)
    private BigDecimal totalAmount;

    @ExcelProperty(value = "订单状态", index = 3)
    @ColumnWidth(12)
    private String statusDescription;

    @ExcelProperty(value = "支付方式", index = 4)
    @ColumnWidth(12)
    private String paymentMethodDescription;

    @ExcelProperty(value = "商品明细", index = 5)
    @ColumnWidth(30)
    private String productDetails;

    @ExcelProperty(value = "创建时间", index = 6)
    @ColumnWidth(20)
    private String createdTime;

    @ExcelProperty(value = "更新时间", index = 7)
    @ColumnWidth(20)
    private String updatedTime;

    @ExcelProperty(value = "备注", index = 8)
    @ColumnWidth(30)
    private String remark;

    /**
     * 从OrderManageDTO转换为OrderExportDTO
     */
    public static OrderExportDTO fromOrderManageDTO(OrderManageDTO orderManageDTO) {
        OrderExportDTO exportDTO = new OrderExportDTO();
        exportDTO.setOrderNo(orderManageDTO.getOrderNo());
        exportDTO.setUsername(orderManageDTO.getUsername());
        exportDTO.setTotalAmount(orderManageDTO.getTotalAmount());
        exportDTO.setStatusDescription(getStatusDescription(orderManageDTO.getStatus()));
        exportDTO.setPaymentMethodDescription(getPaymentMethodDescription(orderManageDTO.getPaymentMethod()));
        exportDTO.setProductDetails(buildProductDetails(orderManageDTO));
        exportDTO.setCreatedTime(orderManageDTO.getCreatedTime() != null ? 
            orderManageDTO.getCreatedTime().toString() : "");
        exportDTO.setUpdatedTime(orderManageDTO.getUpdatedTime() != null ? 
            orderManageDTO.getUpdatedTime().toString() : "");
        exportDTO.setRemark(orderManageDTO.getRemark());
        return exportDTO;
    }

    /**
     * 获取订单状态描述
     */
    private static String getStatusDescription(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 1 -> "待支付";
            case 2 -> "已支付";
            case 3 -> "已发货";
            case 4 -> "已完成";
            case 5 -> "已取消";
            default -> "未知状态";
        };
    }

    /**
     * 获取支付方式描述
     */
    private static String getPaymentMethodDescription(Integer paymentMethod) {
        if (paymentMethod == null) return "未知";
        return switch (paymentMethod) {
            case 1 -> "微信支付";
            case 2 -> "支付宝";
            case 3 -> "银行卡";
            default -> "其他";
        };
    }

    /**
     * 构建商品明细字符串
     */
    private static String buildProductDetails(OrderManageDTO orderManageDTO) {
        if (orderManageDTO.getOrderItems() == null || orderManageDTO.getOrderItems().isEmpty()) {
            return "无明细";
        }

        StringBuilder details = new StringBuilder();
        for (int i = 0; i < orderManageDTO.getOrderItems().size(); i++) {
            OrderManageDTO.OrderItemDTO item = orderManageDTO.getOrderItems().get(i);
            if (i > 0) {
                details.append("; ");
            }
            details.append(String.format("%s x%d (￥%.2f)", 
                item.getProductName(), 
                item.getQuantity(), 
                item.getTotalPrice()));
        }
        return details.toString();
    }
} 