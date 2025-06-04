package com.ssai.lumilovebackend.controller;

import com.ssai.lumilovebackend.dto.LoginRequest;
import com.ssai.lumilovebackend.dto.LoginResponse;
import com.ssai.lumilovebackend.entity.User;
import com.ssai.lumilovebackend.service.UserService;
import com.ssai.lumilovebackend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // 用户注册
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody LoginRequest request) {
        if (userService.findByEmail(request.getEmail()) != null) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("message", "This email has been enrolled"));
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setUsername(request.getEmail().split("@")[0]); // 使用邮箱前缀作为用户名
        user.setBalance(new java.math.BigDecimal("0.00"));

        userService.save(user);

        return ResponseEntity.ok(Collections.singletonMap("message", "注册成功"));
    }

    // 用户登录
    /*
    请求方法: POST
Content-Type: application/json

请求体:
{
  "email": string,    // 用户邮箱
  "password": string  // 用户密码
}

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

失败 (400/401):
{
  "message": string   // 错误信息
}
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userService.findByEmail(request.getEmail());

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("message", "email or password error"));
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        LoginResponse.UserDto userDto = new LoginResponse.UserDto(
            user.getId(),
            user.getUsername(),
            user.getEmail()
        );

        return ResponseEntity.ok(new LoginResponse(token, userDto));
    }
}
