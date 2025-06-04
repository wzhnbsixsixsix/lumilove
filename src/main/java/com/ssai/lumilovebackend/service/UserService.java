package com.ssai.lumilovebackend.service;

import com.ssai.lumilovebackend.entity.User;

public interface UserService {
    User getCurrentUser();
    User findByEmail(String email);
    User save(User user);
    User updateProfile(Long userId, String username, String avatar);
}