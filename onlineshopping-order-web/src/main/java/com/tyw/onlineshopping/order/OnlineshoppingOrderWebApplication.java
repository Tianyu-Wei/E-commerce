package com.tyw.onlineshopping.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.tyw.onlineshopping")
public class OnlineshoppingOrderWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineshoppingOrderWebApplication.class, args);
    }

}
