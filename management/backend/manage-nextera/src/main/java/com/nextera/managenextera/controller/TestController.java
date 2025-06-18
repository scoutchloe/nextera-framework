package com.nextera.managenextera.controller;

import com.nextera.managenextera.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 */
@Slf4j
@RestController
@RequestMapping("/test")
@Tag(name = "测试接口", description = "用于测试前后端连接的接口")
public class TestController {

    @GetMapping("/ping")
    @Operation(summary = "测试连接", description = "测试前后端连接是否正常")
    public Result<Map<String, Object>> ping() {
        log.info("收到ping请求");
        
        Map<String, Object> data = new HashMap<>();
        data.put("message", "pong");
        data.put("timestamp", LocalDateTime.now());
        data.put("server", "manage-nextera");
        data.put("port", 7777);
        
        return Result.success("连接测试成功", data);
    }

    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查服务健康状态")
    public Result<Map<String, Object>> health() {
        log.info("收到健康检查请求");
        
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", LocalDateTime.now());
        data.put("application", "management-nextera");
        
        return Result.success("服务运行正常", data);
    }
} 