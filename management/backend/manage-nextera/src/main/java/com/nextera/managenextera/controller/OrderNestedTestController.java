package com.nextera.managenextera.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.nextera.managenextera.common.Result;
import com.nextera.managenextera.dto.OrderManageDTO;
import com.nextera.managenextera.dto.OrderSearchDTO;
import com.nextera.managenextera.service.impl.OrderManageServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单商品嵌套查询测试控制器
 * 测试使用ElasticsearchTemplate实现的商品嵌套查询功能
 *
 * @author nextera
 * @since 2025-01-01
 */
@Tag(name = "订单商品嵌套查询测试", description = "测试ElasticsearchTemplate商品嵌套查询功能")
@RestController
@RequestMapping("/order/nested-test")
@Slf4j
public class OrderNestedTestController {

    @Autowired
    private OrderManageServiceImpl orderManageService;

    @Operation(summary = "测试基础商品嵌套查询", description = "测试根据商品名称进行基础嵌套查询")
    @PostMapping("/basic-nested-search")
    public Result<IPage<OrderManageDTO>> testBasicNestedSearch(@Validated @RequestBody OrderSearchDTO searchDTO) {
        log.info("测试基础商品嵌套查询: {}", searchDTO);
        
        // 只设置商品名称，触发基础嵌套查询
        if (searchDTO.getProductName() == null) {
            searchDTO.setProductName("测试商品");
        }
        
        IPage<OrderManageDTO> result = orderManageService.getOrderPage(searchDTO);
        return Result.success("基础商品嵌套查询成功", result);
    }

    @Operation(summary = "测试高级商品嵌套查询", description = "测试复杂条件组合的高级嵌套查询")
    @PostMapping("/advanced-nested-search")
    public Result<IPage<OrderManageDTO>> testAdvancedNestedSearch(@Validated @RequestBody OrderSearchDTO searchDTO) {
        log.info("测试高级商品嵌套查询: {}", searchDTO);
        
        // 设置商品名称和其他条件，触发高级嵌套查询
        if (searchDTO.getProductName() == null) {
            searchDTO.setProductName("测试商品");
        }
        if (searchDTO.getOrderNo() == null) {
            searchDTO.setOrderNo("ORDER");
        }
        
        IPage<OrderManageDTO> result = orderManageService.getOrderPage(searchDTO);
        return Result.success("高级商品嵌套查询成功", result);
    }

    @Operation(summary = "测试多条件商品嵌套查询", description = "测试多个商品相关条件的嵌套查询")
    @PostMapping("/multiple-product-conditions")
    public Result<IPage<OrderManageDTO>> testMultipleProductConditions(@Validated @RequestBody OrderSearchDTO searchDTO) {
        log.info("测试多条件商品嵌套查询: {}", searchDTO);
        
        IPage<OrderManageDTO> result = orderManageService.searchOrdersByMultipleProductConditions(searchDTO);
        return Result.success("多条件商品嵌套查询成功", result);
    }

    @Operation(summary = "生成测试数据", description = "生成用于测试的查询条件")
    @PostMapping("/generate-test-data")
    public Result<OrderSearchDTO> generateTestData() {
        log.info("生成测试数据");
        
        OrderSearchDTO testSearchDTO = new OrderSearchDTO();
        
        // 设置基础查询条件
        testSearchDTO.setProductName("iPhone");
        testSearchDTO.setOrderNo("ORDER202501");
        testSearchDTO.setUsername("张三");
        
        // 设置分页
        testSearchDTO.setCurrent(1);
        testSearchDTO.setSize(10);
        
        return Result.success("测试数据生成成功", testSearchDTO);
    }

    @Operation(summary = "对比查询性能", description = "对比基础查询和嵌套查询的性能")
    @PostMapping("/performance-comparison")
    public Result<String> performanceComparison(@Validated @RequestBody OrderSearchDTO searchDTO) {
        log.info("对比查询性能: {}", searchDTO);
        
        long startTime, endTime;
        
        // 测试基础查询
        searchDTO.setProductName(null); // 清除商品名称，触发基础查询
        startTime = System.currentTimeMillis();
        IPage<OrderManageDTO> basicResult = orderManageService.getOrderPage(searchDTO);
        endTime = System.currentTimeMillis();
        long basicQueryTime = endTime - startTime;
        
        // 测试嵌套查询
        searchDTO.setProductName("iPhone"); // 设置商品名称，触发嵌套查询
        startTime = System.currentTimeMillis();
        IPage<OrderManageDTO> nestedResult = orderManageService.getOrderPage(searchDTO);
        endTime = System.currentTimeMillis();
        long nestedQueryTime = endTime - startTime;
        
        String performanceReport = String.format(
            "性能对比报告:\n" +
            "基础查询 - 耗时: %dms, 结果数: %d\n" +
            "嵌套查询 - 耗时: %dms, 结果数: %d\n" +
            "性能差异: %dms",
            basicQueryTime, basicResult.getTotal(),
            nestedQueryTime, nestedResult.getTotal(),
            nestedQueryTime - basicQueryTime
        );
        
        log.info(performanceReport);
        
        return Result.success("性能对比完成", performanceReport);
    }
} 