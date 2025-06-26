package com.nextera.managenextera.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * Elasticsearch配置类
 *
 * @author nextera
 * @since 2025-01-01
 */
@Slf4j
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.nextera.managenextera.repository")
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