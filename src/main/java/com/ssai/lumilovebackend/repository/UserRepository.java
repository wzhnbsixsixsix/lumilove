package com.ssai.lumilovebackend.repository;

import com.ssai.lumilovebackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
JpaRepository 已自动提供的方法包括：
findAll()：查所有用户
findById(Long id)：按主键查
save(User user)：新增或更新
deleteById(Long id)：按 ID 删除
count()：统计数量
等等，无需你自己写 SQL！
 */

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
} 