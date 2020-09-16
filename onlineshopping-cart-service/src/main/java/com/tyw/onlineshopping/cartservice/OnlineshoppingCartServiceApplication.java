package com.tyw.onlineshopping.cartservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@ComponentScan({"com.tyw.onlineshopping"})
@MapperScan(basePackages = "com.tyw.onlineshopping")
public class OnlineshoppingCartServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineshoppingCartServiceApplication.class, args);
    }

}
