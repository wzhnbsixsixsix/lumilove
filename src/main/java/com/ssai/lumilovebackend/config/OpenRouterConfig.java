package com.ssai.lumilovebackend.config;

import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Configuration
public class OpenRouterConfig {

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.api.url}")
    private String appUrl;

    @Value("${openrouter.api.name}")
    private String appName;

    @Value("${openrouter.api.model}")
    private String modelName; // 添加这个字段

    @Value("${openrouter.system-prompt}")
    private String systemPrompt;

    @Bean
    public RestTemplate openRouterRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // Add interceptors to include required headers
        restTemplate.getInterceptors().add((request, body, execution) -> {
            HttpHeaders headers = request.getHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("HTTP-Referer", appUrl);
            headers.set("X-Title", appName);
            return execution.execute(request, body);
        });

        return restTemplate;
    }

    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(10) // 每秒最多10个请求
                .limitRefreshPeriod(Duration.ofSeconds(1))
                .timeoutDuration(Duration.ZERO) // 不等待，直接拒绝
                .build();

        return RateLimiterRegistry.of(config);
    }

    @Bean
    public String openRouterModelName() {
        return modelName; // 修改这里，返回 modelName 而不是 appName
    }
}