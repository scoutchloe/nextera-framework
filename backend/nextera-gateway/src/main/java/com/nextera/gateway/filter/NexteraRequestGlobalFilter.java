package com.nextera.gateway.filter;

import com.nextera.common.constant.CommonConstants;
import com.nextera.gateway.utils.Knife4jUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.addOriginalRequestUrl;

/**
 * 全局拦截器，作用于所有微服务
 * <p>
 * 1. 清洗请求头中的from参数 2. 重写StripPrefix = 1，支持全局路由
 *
 * @author Nextera
 */
@Slf4j
public class NexteraRequestGlobalFilter implements GlobalFilter, Ordered {

	/**
	 * 处理Web请求并（可选地）通过给定的网关过滤器链委托给下一个过滤器
	 * @param exchange 当前服务器交换对象
	 * @param chain 提供委托给下一个过滤器的方式
	 * @return {@code Mono<Void>} 表示请求处理完成
	 */
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

		// 1. 清洗请求头中from 参数
		ServerHttpRequest request = exchange.getRequest().mutate().headers(httpHeaders -> {
			httpHeaders.remove(CommonConstants.FROM);
			// 设置请求时间
			httpHeaders.put(CommonConstants.REQUEST_START_TIME,
					Collections.singletonList(String.valueOf(System.currentTimeMillis())));
		}).build();

		// 2. 重写StripPrefix
		addOriginalRequestUrl(exchange, request.getURI());
		String rawPath = request.getURI().getRawPath();
		log.debug("Processing requestFilter rawPath: {}", rawPath);
		String newPath = rawPath;
		// 3. 对knife4j 文档不处理.
		if(!Knife4jUtil.isKnife4jResource(rawPath)) {
			 newPath = "/" + Arrays.stream(StringUtils.tokenizeToStringArray(rawPath, "/"))
//					.skip(1L)
					.collect(Collectors.joining("/"));
		}
		log.debug("Processing requestFilter newPath: {}", newPath);
		ServerHttpRequest newRequest = request.mutate().path(newPath).build();
		exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, newRequest.getURI());

		return chain.filter(exchange.mutate().request(newRequest.mutate().build()).build());
	}

	@Override
	public int getOrder() {
		return 10;
	}

}
