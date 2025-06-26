package com.nextera.managenextera;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;

@SpringBootApplication(exclude = {RedisRepositoriesAutoConfiguration.class})
@MapperScan("com.nextera.managenextera.mapper")
public class ManageNexteraApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManageNexteraApplication.class, args);
    }

}
