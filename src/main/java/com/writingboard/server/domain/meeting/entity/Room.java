package com.writingboard.server.domain.meeting.entity;

import com.writingboard.server.domain.meeting.entity.enums.ParticipantRole;
import com.writingboard.server.domain.meeting.entity.enums.RoomStatus;
import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.team.entity.Team;
import com.writingboard.server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "room")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Room extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "host_id", nullable = false, foreignKey = @ForeignKey(name = "fk_room_host"))
    private Member host;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false, foreignKey = @ForeignKey(name = "fk_room_team"))
    private Team team;

    @Column(name = "room_uuid", length = 36, nullable = false)
    private String roomUuid;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private RoomStatus status = RoomStatus.OPEN;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "last_activity_at")
    private Instant lastActivityAt;

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomInvite> invites = new ArrayList<>();

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomAsset> assets = new ArrayList<>();

    public static Room create(String roomUuid, String title, Member host, Team team, String passwordHash) {
        Room r = new Room();
        r.roomUuid = roomUuid;
        r.title = title;
        r.host = host;
        r.team = team;
        r.passwordHash = passwordHash;
        r.status = RoomStatus.OPEN;
        r.lastActivityAt = Instant.now();
        return r;
    }

    public RoomParticipant join(Member member, ParticipantRole role) {
        this.lastActivityAt = Instant.now();

        // 이미 참가 기록이 있으면 재입장 처리
        for (RoomParticipant p : participants) {
            if (p.getMember().getId().equals(member.getId())) {
                p.rejoin(role);
                return p;
            }
        }

        RoomParticipant p = RoomParticipant.join(this, member, role);
        participants.add(p);
        return p;
    }

    public void leave(Member member) {
        this.lastActivityAt = Instant.now();
        participants.stream()
                .filter(p -> p.getMember().getId().equals(member.getId()))
                .findFirst()
                .ifPresent(RoomParticipant::leave);
    }

    public void close() {
        this.status = RoomStatus.CLOSED;
        this.closedAt = Instant.now();
        this.lastActivityAt = Instant.now();
    }
}
