package com.ssai.lumilovebackend.service;

import com.ssai.lumilovebackend.dto.OpenRouterRequest;
import com.ssai.lumilovebackend.dto.OpenRouterResponse;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenRouterService {

    private final RestTemplate openRouterRestTemplate;
    private final String openRouterModelName;


    @Value("${openrouter.api.url}")
    private String apiUrl;

    @RateLimiter(name = "openRouterRateLimiter")
    public OpenRouterResponse chat(OpenRouterRequest request) {
        try {

            // 确保请求使用配置的 model
            if (request.getModel() == null) {
                request = OpenRouterRequest.builder()
                        .model(openRouterModelName)
                        .messages(request.getMessages())
                        .temperature(request.getTemperature())
                        .maxTokens(request.getMaxTokens())
                        .build();
            }

            log.info("Sending request to OpenRouter API: {}", request);
            
            HttpEntity<OpenRouterRequest> entity = new HttpEntity<>(request);
            OpenRouterResponse response = openRouterRestTemplate.exchange(
                UriComponentsBuilder.fromHttpUrl(apiUrl).build().toUri(),
                HttpMethod.POST,
                entity,
                OpenRouterResponse.class
            ).getBody();

            log.info("Received response from OpenRouter API: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error calling OpenRouter API", e);
            throw new RuntimeException("Failed to get response from OpenRouter API: " + e.getMessage());
        }
    }
} 