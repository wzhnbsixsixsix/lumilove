package com.ssai.lumilovebackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class OpenRouterRequest {

    @JsonProperty("model")
    private final String model;  // 使用 final 修饰

    private List<Message> messages;


    @Builder.Default
    private double temperature = 0.7;

    @JsonProperty("max_tokens")
    @Builder.Default
    private int maxTokens = 1000;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }
}