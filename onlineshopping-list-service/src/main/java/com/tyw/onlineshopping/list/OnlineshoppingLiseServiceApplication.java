package com.tyw.onlineshopping.list;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@ComponentScan(basePackages = "com.tyw.onlineshopping")
public class OnlineshoppingLiseServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineshoppingLiseServiceApplication.class, args);
    }

}
