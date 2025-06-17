package com.nextera.gateway.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.cloud.gateway.discovery.DiscoveryLocatorProperties;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * @author Scout
 * @date 2025-06-15 20:35
 * @since 1.0
 */
@Configuration
@RefreshScope
public class NexteraDiscoveryClientRouteDefinitionLocator {

    @Bean
    public DiscoveryClientRouteDefinitionLocator dynamicRoutes(ReactiveDiscoveryClient rdc, DiscoveryLocatorProperties dlp) {
        return new DiscoveryClientRouteDefinitionLocator(rdc, dlp);
    }

    @Value("${server.port}")
    private  String serverPort;

    @PostConstruct
    public void init() {
        System.out.println("gateway port:" +  serverPort);
    }
}