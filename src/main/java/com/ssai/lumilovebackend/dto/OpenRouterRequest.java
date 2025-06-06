package com.ssai.lumilovebackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenRouterRequest {
    @Builder.Default
    private String model = "openai/gpt-4";

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