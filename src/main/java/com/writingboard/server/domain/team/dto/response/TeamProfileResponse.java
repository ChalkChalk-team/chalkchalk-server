package com.writingboard.server.domain.team.dto.response;

import com.writingboard.server.domain.team.entity.TeamMember;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamProfileResponse {

    private Long memberId;
    private Long teamId;
    private String nickname;
    private String profileImageUrl;
    private String globalName;

    public static TeamProfileResponse of(TeamMember teamMember, String profileImageUrl) {
        return TeamProfileResponse.builder()
                .memberId(teamMember.getMember().getId())
                .teamId(teamMember.getTeam().getId())
                .nickname(teamMember.getNickname())
                .profileImageUrl(profileImageUrl)
                .globalName(teamMember.getMember().getDisplayName())
                .build();
    }
}
