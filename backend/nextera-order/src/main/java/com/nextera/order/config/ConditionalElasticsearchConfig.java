package com.nextera.order.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * 条件性ElasticSearch配置类
 * 只有在配置了elasticsearch.enabled=true时才启用
 * 
 * @author Nextera Framework
 * @since 1.0.0
 */
@Configuration
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
@EnableElasticsearchRepositories(basePackages = "com.nextera.order.repository")
public class ConditionalElasticsearchConfig extends ElasticsearchConfiguration {

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.create("localhost:9200");
    }
} 