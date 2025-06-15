package com.nextera.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nextera.gateway.filter.NexteraRequestGlobalFilter;
import com.nextera.gateway.handler.NexteraGlobalExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 网关配置类
 *
 * @author Nextera
 */
@Configuration(proxyBeanMethods = false)
public class GatewayConfiguration {

	/**
	 * 创建PigRequest全局过滤器
	 * @return PigRequest全局过滤器
	 */
	@Bean
	public NexteraRequestGlobalFilter nexteraRequestGlobalFilter() {
		return new NexteraRequestGlobalFilter();
	}

	/**
	 * 创建全局异常处理程序
	 * @param objectMapper 对象映射器
	 * @return 全局异常处理程序
	 */
	@Bean
	public NexteraGlobalExceptionHandler globalExceptionHandler(ObjectMapper objectMapper) {
		return new NexteraGlobalExceptionHandler(objectMapper);
	}

}
