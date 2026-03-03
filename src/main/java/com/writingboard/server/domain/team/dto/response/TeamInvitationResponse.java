package com.writingboard.server.domain.team.dto.response;

import com.writingboard.server.domain.team.entity.TeamInvitation;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class TeamInvitationResponse {

    private Long invitationId;
    private Long teamId;
    private String teamName;
    private String inviterName;
    private String inviteeName;
    private String inviteeUserId;
    private String type;
    private String status;
    private Instant expiresAt;
    private Instant createdAt;

    public static TeamInvitationResponse of(TeamInvitation invitation) {
        return TeamInvitationResponse.builder()
                .invitationId(invitation.getId())
                .teamId(invitation.getTeam().getId())
                .teamName(invitation.getTeam().getName())
                .inviterName(invitation.getInviter().getDisplayName())
                .inviteeName(invitation.getInvitee() != null ? invitation.getInvitee().getDisplayName() : null)
                .inviteeUserId(invitation.getInvitee() != null ? invitation.getInvitee().getUserId() : null)
                .type(invitation.getType().name())
                .status(invitation.getStatus().name())
                .expiresAt(invitation.getExpiresAt())
                .createdAt(invitation.getCreatedAt())
                .build();
    }
}
