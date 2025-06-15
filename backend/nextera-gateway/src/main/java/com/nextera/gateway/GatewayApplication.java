package com.nextera.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 网关服务启动类
 *
 * @author Nextera
 */
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.nextera.gateway", "com.nextera.common"})
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);


        System.out.println("serverPort:" + System.getenv("PORT"));
        System.out.println("Nextera Gateway started successfully!");
    }
}