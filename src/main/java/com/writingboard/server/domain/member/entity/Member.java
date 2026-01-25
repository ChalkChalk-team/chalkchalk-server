package com.writingboard.server.domain.member.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String providerId; // 구글의 고유 ID (sub)

    private LocalDateTime createdAt;

    @Builder
    public Member(String email, String name, String providerId) {
        this.email = email;
        this.name = name;
        this.providerId = providerId;
        this.createdAt = LocalDateTime.now();
    }
}