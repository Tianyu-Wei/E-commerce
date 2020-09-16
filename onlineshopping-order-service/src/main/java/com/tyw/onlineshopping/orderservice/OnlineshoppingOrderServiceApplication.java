package com.tyw.onlineshopping.orderservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.tyw.onlineshopping")
@MapperScan(basePackages = "com.tyw.onlineshopping")
@EnableTransactionManagement
public class OnlineshoppingOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineshoppingOrderServiceApplication.class, args);
    }

}
