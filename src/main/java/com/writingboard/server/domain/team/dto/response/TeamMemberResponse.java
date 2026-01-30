package com.writingboard.server.domain.team.dto.response;

import com.writingboard.server.domain.team.entity.TeamMember;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class TeamMemberResponse {

    private Long teamMemberId;
    private Long memberId;
    private String name;
    private String email;
    private String profileImageUrl;
    private String role;
    private String status;
    private Instant joinedAt;

    public static TeamMemberResponse of(TeamMember teamMember) {
        return TeamMemberResponse.builder()
                .teamMemberId(teamMember.getId())
                .memberId(teamMember.getMember().getId())
                .name(teamMember.getMember().getName())
                .email(teamMember.getMember().getEmail())
                .profileImageUrl(teamMember.getMember().getProfileImageUrl())
                .role(teamMember.getRole().name())
                .status(teamMember.getStatus().name())
                .joinedAt(teamMember.getJoinedAt())
                .build();
    }
}
