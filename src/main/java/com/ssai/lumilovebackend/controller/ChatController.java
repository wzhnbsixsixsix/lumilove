package com.ssai.lumilovebackend.controller;

import com.ssai.lumilovebackend.dto.ChatRequest;
import com.ssai.lumilovebackend.dto.ChatResponse;
import com.ssai.lumilovebackend.dto.OpenRouterRequest;
import com.ssai.lumilovebackend.dto.OpenRouterResponse;
import com.ssai.lumilovebackend.service.impl.OpenRouterService;
import com.ssai.lumilovebackend.service.impl.CharacterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
// 移除 @CrossOrigin 注解
// @CrossOrigin(origins = "*", allowedHeaders = "*")
public class ChatController {

    private final OpenRouterService openRouterService;
    private final CharacterService characterService;
    private final String openRouterModelName;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        try {
            log.info("Received chat request: {}", request);

            // 从数据库获取角色prompt
            String characterPrompt = characterService.buildCharacterPrompt(request.getCharacterId());

            // 构建OpenRouter请求
            OpenRouterRequest openRouterRequest = OpenRouterRequest.builder()
                    .model(openRouterModelName)
                    .messages(Arrays.asList(
                            // 使用数据库中的prompt
                            OpenRouterRequest.Message.builder()
                                    .role("system")
                                    .content(characterPrompt)
                                    .build(),
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

            return ResponseEntity.ok(ChatResponse.builder()
                    .success(true)
                    .message(aiMessage)
                    .build());

        } catch (Exception e) {
            log.error("Error processing chat request", e);
            return ResponseEntity.badRequest()
                    .body(ChatResponse.builder()
                            .success(false)
                            .error(e.getMessage())
                            .build());
        }
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<StreamingResponseBody> chatStream(@Valid @RequestBody ChatRequest request) {
        try {
            log.info("Received streaming chat request: {}", request);

            // 验证必要参数
            if (request.getCharacterId() == null) {
                log.error("Missing characterId in request: {}", request);
                return ResponseEntity.badRequest().build();
            }

            // 从数据库获取角色prompt
            String characterPrompt;
            try {
                characterPrompt = characterService.buildCharacterPrompt(request.getCharacterId());
            } catch (Exception e) {
                log.error("Failed to get character prompt for id: {}", request.getCharacterId(), e);
                // 返回更详细的错误信息
                StreamingResponseBody errorStream = outputStream -> {
                    try {
                        String errorMessage = String.format("{\"error\": \"角色ID %d 不存在或数据库查询失败: %s\"}", 
                            request.getCharacterId(), e.getMessage());
                        outputStream.write(("data: " + errorMessage + "\n\n").getBytes());
                        outputStream.flush();
                    } catch (IOException ioException) {
                        log.error("Error writing error message", ioException);
                    }
                };
                return ResponseEntity.ok()
                        .header("Cache-Control", "no-cache")
                        .header("Connection", "keep-alive")
                        .body(errorStream);
            }

            StreamingResponseBody stream = outputStream -> {
                try {
                    OpenRouterRequest openRouterRequest = OpenRouterRequest.builder()
                            .model(openRouterModelName)
                            .messages(Arrays.asList(
                                    OpenRouterRequest.Message.builder()
                                            .role("system")
                                            .content(characterPrompt)
                                            .build(),
                                    OpenRouterRequest.Message.builder()
                                            .role("user")
                                            .content(request.getMessage())
                                            .build()
                            ))
                            .temperature(0.7)
                            .maxTokens(1000)
                            .stream(true)
                            .build();

                    openRouterService.chatStream(openRouterRequest, outputStream);
                } catch (Exception e) {
                    log.error("Error in streaming chat", e);
                    try {
                        outputStream.write(("data: {\"error\": \"" + e.getMessage() + "\"}\n\n").getBytes());
                        outputStream.flush();
                    } catch (IOException ioException) {
                        log.error("Error writing error message", ioException);
                    }
                }
            };

            return ResponseEntity.ok()
                    .header("Cache-Control", "no-cache")
                    .header("Connection", "keep-alive")
                    .body(stream);

        } catch (Exception e) {
            log.error("Error processing streaming chat request", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // 健康检查端点
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Service is healthy");
    }
}