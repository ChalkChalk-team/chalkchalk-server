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

    @Column(name = "invite_token", length = 128, nullable = false)
    private String inviteToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private InviteType type;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "used_count", nullable = false)
    private Integer usedCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private InviteStatus status = InviteStatus.ACTIVE;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "note", length = 255)
    private String note;

    public static RoomInvite issue(Room room, Member issuedBy, String token, InviteType type,
                                   Instant expiresAt, Integer maxUses, String note) {
        RoomInvite i = new RoomInvite();
        i.room = room;
        i.issuedBy = issuedBy;
        i.inviteToken = token;
        i.type = type;
        i.expiresAt = expiresAt;
        i.usedCount = 0;
        i.status = InviteStatus.ACTIVE;
        i.note = note;
        return i;
    }

    public boolean isUsable() {
        if (status == InviteStatus.EXPIRED) return false;
        return expiresAt == null || !expiresAt.isBefore(Instant.now());
    }
}