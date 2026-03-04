package com.writingboard.server.domain.member.entity;

import com.writingboard.server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
        name = "member_device_token",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_member_device_token_apns_token", columnNames = "apns_token")
        },
        indexes = {
                @Index(name = "idx_member_device_token_member_enabled", columnList = "member_id, enabled")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberDeviceToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_token_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "fk_member_device_token_member"))
    private Member member;

    @Column(name = "apns_token", nullable = false, length = 512)
    private String apnsToken;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "last_seen_at", nullable = false)
    private Instant lastSeenAt;

    public static MemberDeviceToken create(Member member, String apnsToken, Instant now) {
        MemberDeviceToken token = new MemberDeviceToken();
        token.member = member;
        token.apnsToken = apnsToken;
        token.enabled = true;
        token.lastSeenAt = now;
        return token;
    }

    public boolean isOwnedBy(Long memberId) {
        return member.getId().equals(memberId);
    }

    public void activate(Instant now) {
        this.enabled = true;
        this.lastSeenAt = now;
    }

    public void deactivate(Instant now) {
        this.enabled = false;
        this.lastSeenAt = now;
    }
}
