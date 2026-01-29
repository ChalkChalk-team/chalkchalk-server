package com.writingboard.server.domain.team.dto.response;

import com.writingboard.server.domain.team.entity.TeamInvitation;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class TeamInviteLinkResponse {

    private Long invitationId;
    private String inviteToken;
    private String inviteUrl;
    private Instant expiresAt;

    public static TeamInviteLinkResponse of(TeamInvitation invitation) {
        return TeamInviteLinkResponse.builder()
                .invitationId(invitation.getId())
                .inviteToken(invitation.getInviteToken())
                .inviteUrl("/api/teams/join/" + invitation.getInviteToken())
                .expiresAt(invitation.getExpiresAt())
                .build();
    }
}
