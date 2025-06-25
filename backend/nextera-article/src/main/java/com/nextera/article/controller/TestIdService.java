package com.nextera.article.controller;

import com.nextera.article.client.IdServiceClient;
import com.nextera.common.core.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Scout
 * @date 2025-06-24 10:45
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/article")
@RequiredArgsConstructor
@Tag(name = "自增长id", description = "自增长id相关接口")
public class TestIdService {

    private final IdServiceClient idServiceClient;

    @GetMapping("generate-id")
    @Operation(summary = "id", description = "创建新id")
    public Result<Long> generateId() {
        log.info("创建id请求: {}",  "generateId");
        return Result.success(idServiceClient.generateId("article"));
    }
}