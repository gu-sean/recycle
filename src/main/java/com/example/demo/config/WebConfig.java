package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 웹에서 /images/product/파일명.jpg 로 접근하면 
        // 실제 컴퓨터의 C:/Users/rnrnd/.../product/ 폴더를 바라보게 합니다.
        registry.addResourceHandler("/images/product/**")
                .addResourceLocations("file:///C:/Users/rnrnd/OneDrive/Desktop/demo/demo/src/main/resources/static/images/product/");
    }
}