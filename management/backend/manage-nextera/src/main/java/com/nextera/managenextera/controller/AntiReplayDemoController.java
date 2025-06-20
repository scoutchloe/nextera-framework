package com.nextera.managenextera.controller;

import com.nextera.managenextera.annotation.AntiReplay;
import com.nextera.managenextera.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 防重放攻击演示控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/anti-replay")
@Tag(name = "防重放攻击演示", description = "展示不同的防重放配置")
public class AntiReplayDemoController {
    
    /**
     * 基础防重放保护 - 只验证时间戳和nonce
     */
    @PostMapping("/basic")
    @Operation(summary = "基础防重放保护", description = "验证时间戳和nonce")
    @AntiReplay
    public Result<Map<String, Object>> basicAntiReplay(@RequestBody Map<String, Object> data) {
        log.info("基础防重放接口被调用，数据: {}", data);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "基础防重放验证通过");
        result.put("timestamp", System.currentTimeMillis());
        result.put("data", data);
        
        return Result.success(result);
    }
    
    /**
     * 序列号防重放保护 - 验证时间戳、nonce和序列号
     */
    @PostMapping("/sequence")
    @Operation(summary = "序列号防重放保护", description = "验证时间戳、nonce和序列号")
    @AntiReplay(enableSequence = true)
    public Result<Map<String, Object>> sequenceAntiReplay(@RequestBody Map<String, Object> data) {
        log.info("序列号防重放接口被调用，数据: {}", data);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "序列号防重放验证通过");
        result.put("timestamp", System.currentTimeMillis());
        result.put("data", data);
        
        return Result.success(result);
    }
    
    /**
     * 签名防重放保护 - 验证时间戳、nonce和签名
     */
    @PostMapping("/signature")
    @Operation(summary = "签名防重放保护", description = "验证时间戳、nonce和签名")
    @AntiReplay(enableSignature = true)
    public Result<Map<String, Object>> signatureAntiReplay(@RequestBody Map<String, Object> data) {
        log.info("签名防重放接口被调用，数据: {}", data);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "签名防重放验证通过");
        result.put("timestamp", System.currentTimeMillis());
        result.put("data", data);
        
        return Result.success(result);
    }
    
    /**
     * 完整防重放保护 - 验证所有参数
     */
    @PostMapping("/full")
    @Operation(summary = "完整防重放保护", description = "验证时间戳、nonce、序列号和签名")
    @AntiReplay(enableSequence = true, enableSignature = true)
    public Result<Map<String, Object>> fullAntiReplay(@RequestBody Map<String, Object> data) {
        log.info("完整防重放接口被调用，数据: {}", data);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "完整防重放验证通过");
        result.put("timestamp", System.currentTimeMillis());
        result.put("data", data);
        
        return Result.success(result);
    }
    
    /**
     * 自定义时间窗口 - 30秒时间窗口
     */
    @PostMapping("/custom-window")
    @Operation(summary = "自定义时间窗口", description = "30秒时间窗口的防重放保护")
    @AntiReplay(timeWindow = 30 * 1000) // 30秒
    public Result<Map<String, Object>> customWindowAntiReplay(@RequestBody Map<String, Object> data) {
        log.info("自定义时间窗口防重放接口被调用，数据: {}", data);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "自定义时间窗口防重放验证通过");
        result.put("timeWindow", "30秒");
        result.put("timestamp", System.currentTimeMillis());
        result.put("data", data);
        
        return Result.success(result);
    }
    
    /**
     * 无保护接口 - 用于对比测试
     */
    @PostMapping("/no-protection")
    @Operation(summary = "无保护接口", description = "没有防重放保护的接口，用于对比测试")
    public Result<Map<String, Object>> noProtection(@RequestBody Map<String, Object> data) {
        log.info("无保护接口被调用，数据: {}", data);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "无保护接口调用成功");
        result.put("timestamp", System.currentTimeMillis());
        result.put("data", data);
        
        return Result.success(result);
    }
    
    /**
     * GET请求防重放保护
     */
    @GetMapping("/get-test")
    @Operation(summary = "GET请求防重放", description = "GET请求的防重放保护")
    @AntiReplay
    public Result<Map<String, Object>> getAntiReplay(@RequestParam(required = false) String param) {
        log.info("GET防重放接口被调用，参数: {}", param);
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "GET防重放验证通过");
        result.put("timestamp", System.currentTimeMillis());
        result.put("param", param);
        
        return Result.success(result);
    }
} 