package com.nextera.managenextera;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.nextera.managenextera.mapper")
public class ManageNexteraApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManageNexteraApplication.class, args);
    }

}
