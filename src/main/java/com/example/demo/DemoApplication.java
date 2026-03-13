package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
// com.example.demo와 db 패키지 둘 다 스캔하도록 설정합니다.
@ComponentScan(basePackages = {"com.example.demo", "db"}) 
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}