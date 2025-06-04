package com.ssai.lumilovebackend.service.impl;

import com.ssai.lumilovebackend.entity.User;
import com.ssai.lumilovebackend.repository.UserRepository;
import com.ssai.lumilovebackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User getCurrentUser() {
        // 示例实现：从 SecurityContext 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                String email = ((UserDetails) principal).getUsername();
                return userRepository.findByEmail(email);
            }
        }
        return null;
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateProfile(Long userId, String username, String avatar) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 检查用户名是否已被其他用户使用
        if (!user.getUsername().equals(username)) {
            User existingUser = userRepository.findByUsername(username);
            if (existingUser != null && !existingUser.getId().equals(userId)) {
                throw new RuntimeException("Username already exists");
            }
        }

        user.setUsername(username);
        user.setAvatar(avatar);
        return userRepository.save(user);
    }
}