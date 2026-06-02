package com.example.drugmanagement;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.example.drugmanagement.mapper")
@SpringBootApplication
public class DrugManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(DrugManagementApplication.class, args);
    }
}
