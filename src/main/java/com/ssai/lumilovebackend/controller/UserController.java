package com.ssai.lumilovebackend.controller;

import com.ssai.lumilovebackend.dto.UpdateProfileRequest;
import com.ssai.lumilovebackend.entity.User;
import com.ssai.lumilovebackend.utils.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ssai.lumilovebackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // 获取当前用户信息
    @GetMapping("/me")
    public User getCurrentUser() {
        return userService.getCurrentUser();
    }


    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request) {
        try {
            // 获取当前登录用户
            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401)
                        .body(Collections.singletonMap("message", "User not authenticated"));
            }

            // 更新用户资料
            User updatedUser = userService.updateProfile(
                    currentUser.getId(),
                    request.getUsername(),
                    request.getAvatar()
            );

            // 返回成功响应
            return ResponseEntity.ok(Collections.singletonMap("message", "Profile updated successfully"));
        } catch (RuntimeException e) {
            // 返回错误响应
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("message", e.getMessage()));
        }
    }
}