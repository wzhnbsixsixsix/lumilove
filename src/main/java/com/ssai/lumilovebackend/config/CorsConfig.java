package com.ssai.lumilovebackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // 匹配所有以 /api 开头的路径
                .allowedOrigins(
                    "http://localhost:3000",                           // 本地开发
                    "https://localhost:3000",                          // 本地HTTPS
                    "https://main.d3m01u43jjmlec.amplifyapp.com"      // 前端域名
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true)  // 允许发送凭证
                .maxAge(3600);
    }
}
