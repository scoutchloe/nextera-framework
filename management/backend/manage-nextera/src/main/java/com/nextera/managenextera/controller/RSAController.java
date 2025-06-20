package com.nextera.managenextera.controller;

import com.nextera.managenextera.util.RSAUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * RSA加密相关接口
 */
@Tag(name = "RSA加密", description = "RSA加密相关接口")
@RestController
@RequestMapping("/rsa")
public class RSAController {

    @Operation(summary = "获取RSA公钥", description = "获取用于前端加密的RSA公钥")
    @GetMapping("/public-key")
    public ResponseEntity<Map<String, Object>> getPublicKey() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "获取成功");
        
        // 构造数据对象
        Map<String, String> data = new HashMap<>();
        data.put("publicKey", RSAUtil.getPublicKey());
        result.put("data", data);
        
        return ResponseEntity.ok(result);
    }
} 