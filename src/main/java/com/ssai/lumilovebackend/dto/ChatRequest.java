package com.ssai.lumilovebackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class ChatRequest {
    @NotNull(message = "Character ID is required")
    private Long characterId;

    @NotBlank(message = "Message cannot be empty")
    private String message;

    @NotBlank(message = "Chat ID cannot be empty")
    private String chatId;
} 