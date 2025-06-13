package com.ssai.lumilovebackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenRouterService {

    private final RestTemplate openRouterRestTemplate;
    private final String openRouterModelName;

    @Value("${openrouter.api.url}")
    private String apiUrl;

    // 添加ObjectMapper作为Bean
    private final ObjectMapper objectMapper = new ObjectMapper();

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

    @RateLimiter(name = "openRouterRateLimiter")
    public void chatStream(OpenRouterRequest request, OutputStream outputStream) {
        try {
            log.info("Sending streaming request to OpenRouter API: {}", request);
            
            HttpEntity<OpenRouterRequest> entity = new HttpEntity<>(request);
            
            // 使用 RestTemplate 的 execute 方法来处理流式响应
            openRouterRestTemplate.execute(
                UriComponentsBuilder.fromHttpUrl(apiUrl).build().toUri(),
                HttpMethod.POST,
                requestCallback -> {
                    requestCallback.getHeaders().addAll(entity.getHeaders());
                    requestCallback.getBody().write(objectMapper.writeValueAsBytes(request));
                },
                responseExtractor -> {
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(responseExtractor.getBody(), StandardCharsets.UTF_8)
                    );
                    
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.debug("Received line: {}", line);
                        
                        // 处理SSE格式的数据行
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6);
                            
                            // 检查是否为结束标志
                            if ("[DONE]".equals(data)) {
                                outputStream.write("data: [DONE]\n\n".getBytes());
                                outputStream.flush();
                                log.info("Stream completed");
                                return null;
                            }
                            
                            // 忽略空数据行
                            if (data.trim().isEmpty()) {
                                continue;
                            }
                            
                            try {
                                // 解析JSON并提取内容
                                JsonNode jsonNode = objectMapper.readTree(data);
                                JsonNode choices = jsonNode.get("choices");
                                if (choices != null && choices.isArray() && choices.size() > 0) {
                                    JsonNode delta = choices.get(0).get("delta");
                                    if (delta != null && delta.has("content")) {
                                        String content = delta.get("content").asText();
                                        if (!content.isEmpty()) {
                                            // 转发到前端
                                            outputStream.write(("data: " + data + "\n\n").getBytes());
                                            outputStream.flush();
                                            log.debug("Streamed content: {}", content);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                log.warn("Failed to parse streaming data: {}", data, e);
                                // 即使解析失败，也转发原始数据，让前端处理
                                outputStream.write(("data: " + data + "\n\n").getBytes());
                                outputStream.flush();
                            }
                        }
                        // 转发注释行（OpenRouter的保持连接注释）
                        else if (line.startsWith(": ")) {
                            outputStream.write((line + "\n").getBytes());
                            outputStream.flush();
                        }
                        // 转发空行
                        else if (line.trim().isEmpty()) {
                            outputStream.write("\n".getBytes());
                            outputStream.flush();
                        }
                    }
                    return null;
                }
            );
            
        } catch (Exception e) {
            log.error("Error in streaming chat", e);
            throw new RuntimeException("Failed to get streaming response from OpenRouter API: " + e.getMessage());
        }
    }
} 