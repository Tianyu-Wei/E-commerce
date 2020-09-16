package com.tyw.onlineshopping.item;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.tyw.onlineshopping")
public class OnlineshoppingItemWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineshoppingItemWebApplication.class, args);
    }

}
