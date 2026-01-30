package com.writingboard.server.domain.team.entity;

import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.team.entity.enums.TeamMemberStatus;
import com.writingboard.server.domain.team.entity.enums.TeamRole;
import com.writingboard.server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "team_member",
        uniqueConstraints = @UniqueConstraint(name = "uk_team_member", columnNames = {"team_id", "member_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_member_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false, foreignKey = @ForeignKey(name = "fk_team_member_team"))
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "fk_team_member_member"))
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, nullable = false)
    private TeamRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private TeamMemberStatus status;

    @Column(name = "joined_at")
    private Instant joinedAt;

    public static TeamMember createOwner(Team team, Member member) {
        TeamMember tm = new TeamMember();
        tm.team = team;
        tm.member = member;
        tm.role = TeamRole.OWNER;
        tm.status = TeamMemberStatus.ACTIVE;
        tm.joinedAt = Instant.now();
        return tm;
    }

    public static TeamMember createMember(Team team, Member member) {
        TeamMember tm = new TeamMember();
        tm.team = team;
        tm.member = member;
        tm.role = TeamRole.MEMBER;
        tm.status = TeamMemberStatus.ACTIVE;
        tm.joinedAt = Instant.now();
        return tm;
    }

    public static TeamMember createPending(Team team, Member member) {
        TeamMember tm = new TeamMember();
        tm.team = team;
        tm.member = member;
        tm.role = TeamRole.MEMBER;
        tm.status = TeamMemberStatus.PENDING;
        tm.joinedAt = null;
        return tm;
    }

    public void activate() {
        this.status = TeamMemberStatus.ACTIVE;
        this.joinedAt = Instant.now();
    }

    public void leave() {
        this.status = TeamMemberStatus.LEFT;
    }

    public void promoteToAdmin() {
        this.role = TeamRole.ADMIN;
    }

    public void demoteToMember() {
        this.role = TeamRole.MEMBER;
    }

    public boolean isOwner() {
        return this.role == TeamRole.OWNER;
    }

    public boolean isAdmin() {
        return this.role == TeamRole.ADMIN;
    }

    public boolean isAdminOrOwner() {
        return this.role == TeamRole.OWNER || this.role == TeamRole.ADMIN;
    }

    public boolean isActive() {
        return this.status == TeamMemberStatus.ACTIVE;
    }
}
