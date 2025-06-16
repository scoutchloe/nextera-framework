package com.nextera.article.controller;

import com.nextera.common.core.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 简化的文章控制器（用于测试）
 */
@RestController
@RequestMapping("/api/simple")
public class SimpleArticleController {

    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("Article service is running!");
    }

    @GetMapping("/test")
    public Result<String> test() {
        return Result.success("Test endpoint works!");
    }
} 