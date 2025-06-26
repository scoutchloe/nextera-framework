package com.nextera.managenextera.service;

import com.nextera.managenextera.dto.OrderExportDTO;
import com.nextera.managenextera.dto.OrderSearchDTO;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Excel导出服务接口
 *
 * @author nextera
 * @since 2025-01-01
 */
public interface ExcelExportService {

    /**
     * 导出订单数据到Excel
     *
     * @param searchDTO 搜索条件
     * @param response  HTTP响应
     * @throws IOException IO异常
     */
    void exportOrders(OrderSearchDTO searchDTO, HttpServletResponse response) throws IOException;

    /**
     * 导出指定订单列表到Excel
     *
     * @param orders   订单列表
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    void exportOrderList(List<OrderExportDTO> orders, HttpServletResponse response) throws IOException;

    /**
     * 导出订单统计数据到Excel
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @param response  HTTP响应
     * @throws IOException IO异常
     */
    void exportOrderStatistics(String startDate, String endDate, HttpServletResponse response) throws IOException;
} 