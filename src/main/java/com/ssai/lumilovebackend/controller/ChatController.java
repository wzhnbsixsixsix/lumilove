package com.ssai.lumilovebackend.controller;

import com.ssai.lumilovebackend.dto.ChatRequest;
import com.ssai.lumilovebackend.dto.ChatResponse;
import com.ssai.lumilovebackend.dto.OpenRouterRequest;
import com.ssai.lumilovebackend.dto.OpenRouterResponse;
import com.ssai.lumilovebackend.service.OpenRouterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
// 移除 @CrossOrigin 注解
// @CrossOrigin(origins = "*", allowedHeaders = "*")
public class ChatController {

    private final OpenRouterService openRouterService;
    private final String openRouterModelName;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        try {
            log.info("Received chat request: {}", request);

            // 构建OpenRouter请求
            OpenRouterRequest openRouterRequest = OpenRouterRequest.builder()
                    .model(openRouterModelName)
                    .messages(Collections.singletonList(
                            OpenRouterRequest.Message.builder()
                                    .role("user")
                                    .content(request.getMessage())
                                    .build()
                    ))
                    .temperature(0.7)
                    .maxTokens(1000)
                    .build();

            // 调用OpenRouter服务
            OpenRouterResponse response = openRouterService.chat(openRouterRequest);

            // 从响应中提取AI回复
            String aiMessage = response.getChoices().get(0).getMessage().getContent();

            // 返回成功响应
            return ResponseEntity.ok(ChatResponse.builder()
                    .success(true)
                    .message(aiMessage)
                    .build());

        } catch (Exception e) {
            log.error("Error processing chat request", e);
            // 返回错误响应
            return ResponseEntity.badRequest()
                    .body(ChatResponse.builder()
                            .success(false)
                            .error(e.getMessage())
                            .build());
        }
    }

    // 健康检查端点
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Service is healthy");
    }
}