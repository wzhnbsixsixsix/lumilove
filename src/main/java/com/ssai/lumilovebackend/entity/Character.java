package com.ssai.lumilovebackend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "character")
@Data // 包括 @Getter, @Setter, @ToString, @EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder // 支持链式构建对象：Character.builder()....
public class Character {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "prompt_config", nullable = false, columnDefinition = "json")
    private String promptConfig;

    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal price = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = true)
    private User creator;

    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = false;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "usage_count")
    @Builder.Default
    private Integer usageCount = 0;

    @Column(name = "like_count")
    @Builder.Default
    private Integer likeCount = 0;

    @Column(name = "is_nsfw")
    @Builder.Default
    private Boolean isNsfw = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_official")
    @Builder.Default
    private Boolean isOfficial = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private CharacterStatus status = CharacterStatus.PENDING;

    @Column(name = "last_active")
    private LocalDateTime lastActive;

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "favorite_count")
    @Builder.Default
    private Integer favoriteCount = 0;

    @Column(name = "chat_count")
    @Builder.Default
    private Integer chatCount = 0;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.price == null) {
            this.price = BigDecimal.ZERO;
        }
        if (this.isPublic == null) {
            this.isPublic = false;
        }
        if (this.usageCount == null) {
            this.usageCount = 0;
        }
        if (this.likeCount == null) {
            this.likeCount = 0;
        }
        if (this.isNsfw == null) {
            this.isNsfw = false;
        }
        if (this.isOfficial == null) {
            this.isOfficial = true;
        }
        if (this.status == null) {
            this.status = CharacterStatus.PENDING;
        }
        if (this.viewCount == null) {
            this.viewCount = 0;
        }
        if (this.favoriteCount == null) {
            this.favoriteCount = 0;
        }
        if (this.chatCount == null) {
            this.chatCount = 0;
        }
    }

    // 角色状态枚举
    public enum CharacterStatus {
        PENDING("pending"),
        APPROVED("approved"),
        REJECTED("rejected"),
        SUSPENDED("suspended");

        private final String value;

        CharacterStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
} 