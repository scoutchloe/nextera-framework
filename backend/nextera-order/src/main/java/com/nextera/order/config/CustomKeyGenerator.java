package com.nextera.order.config;

import com.nextera.order.client.IdServiceClientForOrder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.context.AlgorithmSQLContext;
import org.apache.shardingsphere.infra.algorithm.keygen.core.KeyGenerateAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * 自定义主键生成器，符合ShardingSphere-JDBC 5.5接口规范
 * 使用nextera-id提供的dubbo id服务
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
@Slf4j
@Component
public class CustomKeyGenerator implements KeyGenerateAlgorithm {

    /**
     * 算法类型
     */
    public static final String TYPE = "CUSTOM";

    /**
     * 业务类型属性名
     */
    private static final String BUSINESS_TYPE_PROPERTY = "business-type";

    /**
     * 默认业务类型
     */
    private static final String DEFAULT_BUSINESS_TYPE = "order";

    /**
     * 算法配置属性
     */
    @Getter
    @Setter
    private Properties props = new Properties();

    /**
     * ID生成服务（通过Dubbo调用nextera-id服务）
     * -- SETTER --
     *  设置ID生成服务（用于测试）
     *
     * @param idGeneratorService ID生成服务

     */
    @Autowired
    private IdServiceClientForOrder idServiceClientForOrder;


    /**
     * 业务类型
     * -- GETTER --
     *  获取业务类型
     *
     * @return 业务类型

     */
    @Getter
    private String businessType;

    @Override
    public void init(Properties props) {
        this.props = props;
        this.businessType = props.getProperty(BUSINESS_TYPE_PROPERTY, DEFAULT_BUSINESS_TYPE);
        log.info("CustomKeyGenerator initialized with business type: {}", businessType);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * 降级ID生成方法
     * 当dubbo服务不可用时使用本地时间戳+随机数生成ID
     *
     * @return 生成的ID
     */
    private Long generateFallbackId() {
        // 使用时间戳(毫秒) + 4位随机数作为降级方案
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 10000);
        Long fallbackId = timestamp * 10000 + random;
        log.warn("Generated fallback ID: {} for business type: {}", fallbackId, businessType);
        return fallbackId;
    }

    /**
     * 获取算法配置
     *
     * @return 算法配置
     */
    public static AlgorithmConfiguration getAlgorithmConfiguration(String businessType) {
        Properties props = new Properties();
        props.setProperty(BUSINESS_TYPE_PROPERTY, businessType);
        return new AlgorithmConfiguration(TYPE, props);
    }

    @Override
    public Collection<? extends Comparable<?>> generateKeys(AlgorithmSQLContext algorithmSQLContext, int keyCount) {
        List<Long> keys = new ArrayList<>();
        
        try {
            // 检查ID服务是否可用
            if (idServiceClientForOrder == null) {
                log.warn("IdGeneratorService is not available, using fallback ID generation for {} keys", keyCount);
                for (int i = 0; i < keyCount; i++) {
                    keys.add(generateFallbackId());
                    // 确保时间戳不同
                    if (i < keyCount - 1) {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
                return keys;
            }

            // 尝试批量生成ID
            try {
                List<Long> batchIds = idServiceClientForOrder.generateIds(businessType, keyCount);
                if (batchIds != null && batchIds.size() == keyCount) {
                    log.debug("Generated {} IDs using batch service for business type: {}", keyCount, businessType);
                    return batchIds;
                }
            } catch (Exception e) {
                log.warn("Batch ID generation failed, falling back to single generation: {}", e.getMessage());
            }

            // 降级到单个生成
            for (int i = 0; i < keyCount; i++) {
                try {
                    Long id = idServiceClientForOrder.generateId(businessType);
                    keys.add(id);
                } catch (Exception e) {
                    log.warn("Single ID generation failed at index {}, using fallback: {}", i, e.getMessage());
                    keys.add(generateFallbackId());
                }
            }
            
            log.debug("Generated {} IDs for business type: {}", keys.size(), businessType);
            return keys;
            
        } catch (Exception e) {
            log.error("Failed to generate {} IDs using IdGeneratorService, falling back to local generation", keyCount, e);
            
            // 完全降级方案
            keys.clear();
            for (int i = 0; i < keyCount; i++) {
                keys.add(generateFallbackId());
                // 确保时间戳不同
                if (i < keyCount - 1) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            return keys;
        }
    }

    @Override
    public boolean isSupportAutoIncrement() {
        // 我们的实现不依赖数据库的自增字段
        return false;
    }
}