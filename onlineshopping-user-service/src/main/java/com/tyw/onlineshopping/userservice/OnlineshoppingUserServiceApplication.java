package com.tyw.onlineshopping.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.tyw.onlineshopping.userservice.mapper")
@ComponentScan(basePackages = "com.tyw.onlineshopping")
public class OnlineshoppingUserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineshoppingUserServiceApplication.class, args);
    }

}
