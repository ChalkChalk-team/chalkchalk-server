package com.writingboard.server.domain.team.controller;

import com.writingboard.server.domain.team.dto.request.RoleChangeRequest;
import com.writingboard.server.domain.team.dto.response.TeamMemberResponse;
import com.writingboard.server.domain.team.service.TeamMemberService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams/{teamId}/members")
@RequiredArgsConstructor
public class TeamMemberController {

    private final TeamMemberService teamMemberService;

    @GetMapping
    @Operation(summary = "팀 멤버 목록 조회", description = "특정 팀에 속한 모든 멤버의 목록을 조회한다.")
    public ResponseEntity<List<TeamMemberResponse>> getTeamMembers(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId) {

        List<TeamMemberResponse> teamMembers = teamMemberService.getTeamMembers(teamId, memberId);
        return ResponseEntity.ok(teamMembers);
    }

    @DeleteMapping("/me")
    @Operation(summary = "팀 탈퇴", description = "현재 인증된 사용자가 특정 팀에서 탈퇴한다.")
    public ResponseEntity<Void> leaveTeam(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId) {

        teamMemberService.leaveTeam(memberId, teamId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{targetMemberId}")
    @Operation(summary = "팀 멤버 강제 탈퇴", description = "특정 팀 멤버를 팀에서 강제로 탈퇴시킨다.")
    public ResponseEntity<Void> kickMember(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @PathVariable Long targetMemberId) {

        teamMemberService.kickMember(memberId, teamId, targetMemberId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{targetMemberId}/role")
    @Operation(summary = "팀 멤버 역할 변경", description = "특정 팀 멤버의 역할을 변경한다.")
    public ResponseEntity<TeamMemberResponse> changeRole(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @PathVariable Long targetMemberId,
            @RequestBody @Valid RoleChangeRequest request) {

        TeamMemberResponse updatedMember = teamMemberService.changeRole(memberId, teamId, targetMemberId, request);
        return ResponseEntity.ok(updatedMember);
    }
}
