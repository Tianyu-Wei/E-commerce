package com.tyw.onlineshopping.manage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.tyw.onlineshopping")
@EnableTransactionManagement
@ComponentScan({"com.tyw.onlineshopping"})
public class OnlineshoppingManageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnlineshoppingManageServiceApplication.class, args);
    }

}
