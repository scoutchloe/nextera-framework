package com.nextera.gateway.config;

import com.nextera.gateway.utils.Knife4jUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Knife4j配置类
 * 配置API文档聚合，支持webflux
 * 
 * @author Nextera
 * @date 2025-06-16
 */
@Slf4j
@Configuration
@ConditionalOnProperty(value = "knife4j.gateway.enabled", havingValue = "true")
public class Knife4jConfiguration {

    /**
     * 配置CORS过滤器，支持knife4j跨域访问
     */
    @Bean
    public CorsWebFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(Boolean.TRUE);
        config.addAllowedMethod("*");
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.setMaxAge(18000L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }

    /**
     * 支持knife4j静态资源访问的过滤器
     */
    @Bean
    public WebFilter knife4jResourceFilter() {
        return new WebFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
                ServerHttpRequest request = exchange.getRequest();
                String path = request.getURI().getPath();
                
                // 对于knife4j相关的静态资源请求，添加特殊处理
                if (Knife4jUtil.isKnife4jResource(path)) {
                    log.debug("Processing knife4j resource request: {}", path);
                    ServerHttpResponse response = exchange.getResponse();
                    
                    // 设置响应头，允许访问
                    response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
                    response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "*");
                    response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
                    response.getHeaders().add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, "18000");
                }
                
                return chain.filter(exchange);
            }
            

        };
    }
} 