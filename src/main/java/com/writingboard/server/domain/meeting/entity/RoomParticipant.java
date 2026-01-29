package com.writingboard.server.domain.meeting.entity;

import com.writingboard.server.domain.meeting.entity.enums.ParticipantRole;
import com.writingboard.server.domain.meeting.entity.enums.ParticipantState;
import com.writingboard.server.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "room_participant")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoomParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false, foreignKey = @ForeignKey(name = "fk_participant_room"))
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "fk_participant_member"))
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private ParticipantRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 20, nullable = false)
    private ParticipantState state;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    @Column(name = "left_at")
    private Instant leftAt;

    @Column(name = "last_seen_at")
    private Instant lastSeenAt;

    public static RoomParticipant join(Room room, Member member, ParticipantRole role) {
        RoomParticipant p = new RoomParticipant();
        p.room = room;
        p.member = member;
        p.role = role;
        p.state = ParticipantState.JOINED;
        p.joinedAt = Instant.now();
        p.lastSeenAt = p.joinedAt;
        return p;
    }

    public void rejoin(ParticipantRole role) {
        this.role = role != null ? role : this.role;
        this.state = ParticipantState.JOINED;
        this.leftAt = null;
        this.joinedAt = Instant.now();
        this.lastSeenAt = this.joinedAt;
    }

    public void leave() {
        this.state = ParticipantState.LEFT;
        this.leftAt = Instant.now();
    }

    public void heartbeat() {
        this.lastSeenAt = Instant.now();
    }
}