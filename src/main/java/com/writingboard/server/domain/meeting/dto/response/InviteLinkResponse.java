package com.writingboard.server.domain.meeting.dto.response;

import com.writingboard.server.domain.meeting.entity.RoomInvite;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class InviteLinkResponse {

    private String inviteToken;
    private String inviteUrl;
    private Instant expiresAt;

    public static InviteLinkResponse of(RoomInvite invite) {
        return InviteLinkResponse.builder()
                .inviteToken(invite.getInviteToken())
                .inviteUrl("/join/" + invite.getInviteToken())
                .expiresAt(invite.getExpiresAt())
                .build();
    }
}
