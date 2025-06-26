package com.nextera.managenextera.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.nextera.managenextera.dto.OrderExportDTO;
import com.nextera.managenextera.dto.OrderManageDTO;
import com.nextera.managenextera.dto.OrderSearchDTO;
import com.nextera.managenextera.service.ExcelExportService;
import com.nextera.managenextera.service.OrderManageService;
import com.nextera.managenextera.service.OrderStatisticsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel导出服务实现类
 *
 * @author nextera
 * @since 2025-01-01
 */
@Slf4j
@Service
public class ExcelExportServiceImpl implements ExcelExportService {

    @Autowired
    private OrderManageService orderManageService;

    @Autowired
    private OrderStatisticsService orderStatisticsService;

    @Override
    public void exportOrders(OrderSearchDTO searchDTO, HttpServletResponse response) throws IOException {
        log.info("开始导出订单数据: {}", searchDTO);

        try {
            // 获取订单数据
            List<OrderManageDTO> orders = orderManageService.exportOrders(searchDTO);
            
            // 转换为导出DTO
            List<OrderExportDTO> exportList = orders.stream()
                .map(OrderExportDTO::fromOrderManageDTO)
                .toList();

            // 设置响应头
            String fileName = "订单数据_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            setupExcelResponse(response, fileName);

            // 导出Excel
            EasyExcel.write(response.getOutputStream(), OrderExportDTO.class)
                .registerWriteHandler(createCellStyleStrategy())
                .sheet("订单数据")
                .doWrite(exportList);

            log.info("订单数据导出完成，共导出 {} 条记录", exportList.size());

        } catch (Exception e) {
            log.error("导出订单数据失败", e);
            throw new IOException("导出订单数据失败: " + e.getMessage());
        }
    }

    @Override
    public void exportOrderList(List<OrderExportDTO> orders, HttpServletResponse response) throws IOException {
        log.info("开始导出指定订单列表，共 {} 条记录", orders.size());

        try {
            // 设置响应头
            String fileName = "订单列表_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            setupExcelResponse(response, fileName);

            // 导出Excel
            EasyExcel.write(response.getOutputStream(), OrderExportDTO.class)
                .registerWriteHandler(createCellStyleStrategy())
                .sheet("订单列表")
                .doWrite(orders);

            log.info("订单列表导出完成");

        } catch (Exception e) {
            log.error("导出订单列表失败", e);
            throw new IOException("导出订单列表失败: " + e.getMessage());
        }
    }

    @Override
    public void exportOrderStatistics(String startDate, String endDate, HttpServletResponse response) throws IOException {
        log.info("开始导出订单统计数据: {} - {}", startDate, endDate);

        try {
            // 这里可以根据需要创建统计数据的导出格式
            // 暂时使用简单的列表格式
            List<StatisticsExportDTO> statisticsList = new ArrayList<>();

            // 添加概览统计
            var overview = orderStatisticsService.getOverviewStatistics();
            statisticsList.add(new StatisticsExportDTO("今日订单数", overview.getTodayOrderCount().toString()));
            statisticsList.add(new StatisticsExportDTO("今日订单金额", overview.getTodayOrderAmount().toString()));
            statisticsList.add(new StatisticsExportDTO("本月订单数", overview.getMonthOrderCount().toString()));
            statisticsList.add(new StatisticsExportDTO("本月订单金额", overview.getMonthOrderAmount().toString()));
            statisticsList.add(new StatisticsExportDTO("总订单数", overview.getTotalOrderCount().toString()));
            statisticsList.add(new StatisticsExportDTO("总订单金额", overview.getTotalOrderAmount().toString()));

            // 设置响应头
            String fileName = "订单统计_" + startDate + "_" + endDate + "_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            setupExcelResponse(response, fileName);

            // 导出Excel
            EasyExcel.write(response.getOutputStream(), StatisticsExportDTO.class)
                .registerWriteHandler(createCellStyleStrategy())
                .sheet("订单统计")
                .doWrite(statisticsList);

            log.info("订单统计数据导出完成");

        } catch (Exception e) {
            log.error("导出订单统计数据失败", e);
            throw new IOException("导出订单统计数据失败: " + e.getMessage());
        }
    }

    /**
     * 设置Excel响应头
     */
    private void setupExcelResponse(HttpServletResponse response, String fileName) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        
        // 解决中文文件名乱码问题
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
            .replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + encodedFileName);
    }

    /**
     * 创建单元格样式策略
     */
    private HorizontalCellStyleStrategy createCellStyleStrategy() {
        // 头部样式
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        headWriteCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headWriteCellStyle.setFillPatternType(FillPatternType.SOLID_FOREGROUND);
        
        WriteFont headWriteFont = new WriteFont();
        headWriteFont.setFontName("微软雅黑");
        headWriteFont.setFontHeightInPoints((short) 11);
        headWriteFont.setBold(true);
        headWriteCellStyle.setWriteFont(headWriteFont);

        // 内容样式
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        WriteFont contentWriteFont = new WriteFont();
        contentWriteFont.setFontName("微软雅黑");
        contentWriteFont.setFontHeightInPoints((short) 10);
        contentWriteCellStyle.setWriteFont(contentWriteFont);

        return new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
    }

    /**
     * 统计数据导出DTO
     */
    public static class StatisticsExportDTO {
        @com.alibaba.excel.annotation.ExcelProperty(value = "统计项目", index = 0)
        @com.alibaba.excel.annotation.write.style.ColumnWidth(20)
        private String statisticsName;

        @com.alibaba.excel.annotation.ExcelProperty(value = "统计值", index = 1)
        @com.alibaba.excel.annotation.write.style.ColumnWidth(20)
        private String statisticsValue;

        public StatisticsExportDTO() {}

        public StatisticsExportDTO(String statisticsName, String statisticsValue) {
            this.statisticsName = statisticsName;
            this.statisticsValue = statisticsValue;
        }

        public String getStatisticsName() {
            return statisticsName;
        }

        public void setStatisticsName(String statisticsName) {
            this.statisticsName = statisticsName;
        }

        public String getStatisticsValue() {
            return statisticsValue;
        }

        public void setStatisticsValue(String statisticsValue) {
            this.statisticsValue = statisticsValue;
        }
    }
} 