package com.nextera.order.client;

import com.nextera.idapi.service.IdGeneratorService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Scout
 * @date 2025-06-24 23:15
 * @since 1.0
 */
@Component
public class IdServiceClientForOrder {

    @DubboReference(version = "1.0.0", check = false, timeout = 5000)
    private IdGeneratorService idGeneratorService;

    public List<Long> generateIds(String businessType, int count) {
        return idGeneratorService.generateIds(businessType, count);
    }

    public Long generateId(String businessType) {
        return idGeneratorService.generateId(businessType);
    }
}