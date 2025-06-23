package com.nextera.user.service;

import com.nextera.user.client.ArticleServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.ReferenceConfig;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * Dubbo Seata过滤器配置验证器
 * 用于验证RocketMQ场景下的Dubbo Reference是否正确排除了Seata过滤器
 *
 * @author nextera
 * @since 2025-06-23
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DubboSeataFilterVerifier {

//    private final ArticleServiceClient articleServiceClient;
//
//    @PostConstruct
//    public void verifyDubboFilterConfiguration() {
//        log.info("=== 开始验证Dubbo Seata过滤器配置 ===");
//
//        try {
//            // 通过反射检查ArticleServiceClient中的Dubbo Reference配置
//            Class<?> clientClass = articleServiceClient.getClass();
//            Field[] fields = clientClass.getDeclaredFields();
//
//            for (Field field : fields) {
//                if (field.getName().equals("articleServiceForRocketMQ")) {
//                    field.setAccessible(true);
//                    log.info("✅ 找到RocketMQ专用的Dubbo Reference: {}", field.getName());
//
//                    // 检查注解配置
//                    if (field.isAnnotationPresent(org.apache.dubbo.config.annotation.DubboReference.class)) {
//                        org.apache.dubbo.config.annotation.DubboReference dubboRef =
//                            field.getAnnotation(org.apache.dubbo.config.annotation.DubboReference.class);
//
//                        String filterConfig = Arrays.toString(dubboRef.filter());
//                        log.info("✅ RocketMQ专用Dubbo Reference过滤器配置: {}", filterConfig);
//
//                        // 验证是否包含必要的排除项
//                        if (filterConfig.contains("-seataTransactionPropagation") &&
//                            filterConfig.contains("-seata")) {
//                            log.info("✅ Seata过滤器排除配置正确");
//                        } else {
//                            log.warn("⚠️  Seata过滤器排除配置可能不完整: {}", filterConfig);
//                        }
//                    }
//                }
//            }
//
//            log.info("=== Dubbo Seata过滤器配置验证完成 ===");
//
//        } catch (Exception e) {
//            log.error("验证Dubbo过滤器配置时发生异常", e);
//        }
//    }
//
//    /**
//     * 运行时验证方法
//     * 可以通过接口调用来检查配置是否生效
//     */
//    public String verifyFilterConfigurationAtRuntime() {
//        StringBuilder report = new StringBuilder();
//        report.append("=== Dubbo Seata过滤器运行时验证报告 ===\n");
//
//        try {
//            // 这里可以添加更多的运行时检查逻辑
//            report.append("✅ ArticleServiceClient注入成功\n");
//            report.append("✅ RocketMQ专用Dubbo Reference配置已加载\n");
//            report.append("✅ Seata过滤器排除配置应该已生效\n");
//
//            // 如果需要，可以尝试一个轻量级的调用来验证
//            // 但要小心不要触发实际的业务逻辑
//
//        } catch (Exception e) {
//            report.append("❌ 验证过程中发生异常: ").append(e.getMessage()).append("\n");
//        }
//
//        report.append("=== 验证报告结束 ===");
//        return report.toString();
//    }
} 