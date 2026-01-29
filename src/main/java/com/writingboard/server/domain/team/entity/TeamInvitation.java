package com.writingboard.server.domain.team.entity;

import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.team.entity.enums.TeamInvitationStatus;
import com.writingboard.server.domain.team.entity.enums.TeamInvitationType;
import com.writingboard.server.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "team_invitation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamInvitation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invite_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "team_id", nullable = false, foreignKey = @ForeignKey(name = "fk_team_invitation_team"))
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inviter_member_id", nullable = false, foreignKey = @ForeignKey(name = "fk_team_invitation_inviter"))
    private Member inviter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitee_member_id", foreignKey = @ForeignKey(name = "fk_team_invitation_invitee"))
    private Member invitee;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private TeamInvitationType type;

    @Column(name = "invite_token", length = 128, unique = true)
    private String inviteToken;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private TeamInvitationStatus status;

    public static TeamInvitation createLinkInvitation(Team team, Member inviter, String token, Instant expiresAt) {
        TeamInvitation invitation = new TeamInvitation();
        invitation.team = team;
        invitation.inviter = inviter;
        invitation.invitee = null;
        invitation.type = TeamInvitationType.LINK;
        invitation.inviteToken = token;
        invitation.expiresAt = expiresAt;
        invitation.status = TeamInvitationStatus.PENDING;
        return invitation;
    }

    public static TeamInvitation createDirectInvitation(Team team, Member inviter, Member invitee) {
        TeamInvitation invitation = new TeamInvitation();
        invitation.team = team;
        invitation.inviter = inviter;
        invitation.invitee = invitee;
        invitation.type = TeamInvitationType.USER_SEARCH;
        invitation.inviteToken = null;
        invitation.expiresAt = null;
        invitation.status = TeamInvitationStatus.PENDING;
        return invitation;
    }

    public void accept() {
        this.status = TeamInvitationStatus.ACCEPTED;
    }

    public void reject() {
        this.status = TeamInvitationStatus.REJECTED;
    }

    public void expire() {
        this.status = TeamInvitationStatus.EXPIRED;
    }

    public boolean isUsable() {
        if (status != TeamInvitationStatus.PENDING) {
            return false;
        }
        return expiresAt == null || !expiresAt.isBefore(Instant.now());
    }

    public void checkAndExpire() {
        if (status != TeamInvitationStatus.PENDING) {
            return;
        }
        if (expiresAt != null && expiresAt.isBefore(Instant.now())) {
            this.status = TeamInvitationStatus.EXPIRED;
        }
    }

    public boolean isLinkInvitation() {
        return this.type == TeamInvitationType.LINK;
    }

    public boolean isDirectInvitation() {
        return this.type == TeamInvitationType.USER_SEARCH;
    }
}
