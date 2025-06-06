package com.ssai.lumilovebackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChatRequest {
    @NotBlank(message = "Message cannot be empty")
    private String message;

    @NotBlank(message = "Chat ID cannot be empty")
    private String chatId;
} 