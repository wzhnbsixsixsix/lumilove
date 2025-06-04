package com.ssai.lumilovebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
响应:
成功 (200):
{
  "token": string,    // JWT token
  "user": {
    "id": string,
    "username": string,
    "email": string
  }
}
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private UserDto user;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDto {
        private Long id;
        private String username;
        private String email;
        private String avatar;
    }
}

