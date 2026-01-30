package com.writingboard.server.domain.team.dto.response;

import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.team.entity.Team;
import com.writingboard.server.domain.team.entity.TeamMember;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class TeamResponse {

    private Long teamId;
    private String name;
    private String description;
    private String teamProfileImgUrl;
    private CreatorInfo createdBy;
    private List<TeamMemberResponse> members;
    private Instant createdAt;
    private Instant updatedAt;

    public static TeamResponse of(Team team, List<TeamMember> members) {
        return TeamResponse.builder()
                .teamId(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .teamProfileImgUrl(team.getTeamProfileImgUrl())
                .createdBy(CreatorInfo.of(team.getCreatedBy()))
                .members(members.stream()
                        .map(TeamMemberResponse::of)
                        .toList())
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .build();
    }

    public static TeamResponse ofSimple(Team team) {
        return TeamResponse.builder()
                .teamId(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .teamProfileImgUrl(team.getTeamProfileImgUrl())
                .createdBy(CreatorInfo.of(team.getCreatedBy()))
                .members(null)
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .build();
    }

    @Data
    @Builder
    public static class CreatorInfo {
        private Long memberId;
        private String name;
        private String email;

        public static CreatorInfo of(Member member) {
            return CreatorInfo.builder()
                    .memberId(member.getId())
                    .name(member.getName())
                    .email(member.getEmail())
                    .build();
        }
    }
}
