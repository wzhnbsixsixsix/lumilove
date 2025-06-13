package com.ssai.lumilovebackend.repository;

import com.ssai.lumilovebackend.entity.Character;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CharacterRepository extends JpaRepository<Character, Long> {
    
    // 根据名称查找角色
    Optional<Character> findByName(String name);
    
    // 查找所有公开的角色
    List<Character> findByIsPublicTrue();
    
    // 查找所有官方角色
    List<Character> findByIsOfficialTrue();
    
    // 根据状态查找角色
    List<Character> findByStatus(Character.CharacterStatus status);
    
    // 根据创建者查找角色
    List<Character> findByCreatorId(Long creatorId);
    
    // 查找热门角色（按like_count排序）
    @Query("SELECT c FROM Character c WHERE c.isPublic = true ORDER BY c.likeCount DESC")
    List<Character> findPopularCharacters();
    
    // 更新头像URL
    @Query("UPDATE Character c SET c.avatarUrl = :avatarUrl WHERE c.id = :id")
    void updateAvatarUrl(@Param("id") Long id, @Param("avatarUrl") String avatarUrl);
}