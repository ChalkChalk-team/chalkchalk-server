package com.writingboard.server.domain.team.dto.response;

import com.writingboard.server.domain.team.entity.Team;
import com.writingboard.server.domain.team.entity.TeamMember;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class TeamSummaryResponse {

    private Long teamId;
    private String name;
    private String description;
    private String teamProfileImgUrl;
    private String myRole;
    private int memberCount;
    private Instant createdAt;

    public static TeamSummaryResponse of(Team team, TeamMember myMembership, int memberCount) {
        return TeamSummaryResponse.builder()
                .teamId(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .teamProfileImgUrl(team.getTeamProfileImgUrl())
                .myRole(myMembership.getRole().name())
                .memberCount(memberCount)
                .createdAt(team.getCreatedAt())
                .build();
    }
}
