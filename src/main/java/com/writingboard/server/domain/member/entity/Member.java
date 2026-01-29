package com.writingboard.server.domain.member.entity;

import com.writingboard.server.domain.member.entity.enums.MemberStatus;
import com.writingboard.server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // create 메서드 사용 유도
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl; // 지금은 안 씀

    @Column(name = "provider_id", length = 100, nullable = false, unique = true)
    private String providerId; // Guest: deviceId, OAuth: provider user ID

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private MemberStatus status = MemberStatus.ACTIVE;

    /**
     * 게스트 사용자 생성
     */
    public static Member createGuest(String deviceId, String name) {
        Member member = new Member();
        member.email = "guest-" + deviceId.substring(0, Math.min(8, deviceId.length())) + "@temp.local";
        member.name = name != null && !name.isBlank() ? name : "Guest-" + deviceId.substring(0, 6).toUpperCase();
        member.providerId = deviceId;
        member.profileImageUrl = null;
        member.status = MemberStatus.ACTIVE;
        return member;
    }

    /**
     * OAuth 사용자 생성
     */
    public static Member create(String email, String name, String providerId, String profileImageUrl) {
        Member member = new Member();
        member.email = email;
        member.name = name;
        member.providerId = providerId;
        member.profileImageUrl = profileImageUrl;
        member.status = MemberStatus.ACTIVE;
        return member;
    }


    public void updateProfile(String name, String profileImageUrl) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (profileImageUrl != null && !profileImageUrl.isBlank()) {
            this.profileImageUrl = profileImageUrl;
        }
    }

    public void withdraw() {
        this.status = MemberStatus.DELETED;
    }

    public boolean isDeleted() {
        return this.status == MemberStatus.DELETED;
    }
}