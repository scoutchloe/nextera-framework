package com.nextera.article.client;

import com.nextera.idapi.service.IdGeneratorService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/**
 * @author Scout
 * @date 2025-06-24 10:38
 * @since 1.0
 */
@Component
public class IdServiceClient {

    @DubboReference(version = "1.0.0", check = false, timeout = 5000)
    private IdGeneratorService idGeneratorService;

    public Long generateId(String businessType) {
        return idGeneratorService.generateId(businessType);
    }
}