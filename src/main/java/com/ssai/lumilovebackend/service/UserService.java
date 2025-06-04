package com.ssai.lumilovebackend.service;

import com.ssai.lumilovebackend.entity.User;
import org.springframework.stereotype.Service;

public interface UserService {

    
    // 获取当前用户信息业务逻辑
    User getCurrentUser();

    User findByEmail(String email);

    User save(User user);
}