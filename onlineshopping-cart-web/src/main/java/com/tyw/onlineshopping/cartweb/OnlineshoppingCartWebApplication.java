package com.tyw.onlineshopping.cartweb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.tyw.onlineshopping"})
public class OnlineshoppingCartWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineshoppingCartWebApplication.class, args);
    }

}
