package com.writingboard.server.domain.meeting.entity;

import com.writingboard.server.domain.meeting.entity.enums.InviteStatus;
import com.writingboard.server.domain.meeting.entity.enums.InviteType;
import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "room_invite")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoomInvite extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invite_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false, foreignKey = @ForeignKey(name = "fk_invite_room"))
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "issued_by", nullable = false, foreignKey = @ForeignKey(name = "fk_invite_issuer"))
    private Member issuedBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "used_by", foreignKey = @ForeignKey(name = "fk_invite_user"))
    private Member usedBy;

    @Column(name = "invite_token", length = 128, nullable = false)
    private String inviteToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private InviteType type;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private InviteStatus status = InviteStatus.ACTIVE;

    public static RoomInvite issue(Room room, Member issuedBy, Member usedBy, String token, InviteType type,
                                   Instant expiresAt) {
        RoomInvite i = new RoomInvite();
        i.room = room;
        i.issuedBy = issuedBy;
        i.usedBy = usedBy;
        i.inviteToken = token;
        i.type = type;
        i.expiresAt = expiresAt;
        i.status = InviteStatus.ACTIVE;
        return i;
    }

    public boolean isUsable() {
        if (status == InviteStatu.EXPIRED) return false;
        return expiresAt == null || !expiresAt.isBefore(Instant.now());
    }

    /**
     * 초대가 만료됐는지 체크하고, 만료됐으면 상태를 EXPIRED로 변경
     * DB Fallback 시 호출
     */
    public void checkAndExpire() {
        if (status == InviteStatus.EXPIRED) {
            return;
        }

        if (expiresAt != null && expiresAt.isBefore(Instant.now())) {
            this.status = InviteStatus.EXPIRED;
            this.expiresAt = Instant.now();
        }
    }
}