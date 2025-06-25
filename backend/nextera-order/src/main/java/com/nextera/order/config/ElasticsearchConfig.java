package com.nextera.order.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * ElasticSearch配置类
 *
 * @author Nextera Framework
 * @since 1.0.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
@EnableElasticsearchRepositories(basePackages = "com.nextera.order.repository")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris:http://localhost:9200}")
    private String elasticsearchUris;

    @Override
    public ClientConfiguration clientConfiguration() {
        log.info("配置ElasticSearch客户端，连接地址: {}", elasticsearchUris);
        
        return ClientConfiguration.builder()
                .connectedTo(elasticsearchUris.replace("http://", "").replace("https://", ""))
                .withConnectTimeout(10000)
                .withSocketTimeout(30000)
                .build();
    }
} 