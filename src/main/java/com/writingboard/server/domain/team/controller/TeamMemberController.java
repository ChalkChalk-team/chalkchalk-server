package com.writingboard.server.domain.team.controller;

import com.writingboard.server.domain.team.dto.request.RoleChangeRequest;
import com.writingboard.server.domain.team.dto.response.TeamMemberResponse;
import com.writingboard.server.domain.team.service.TeamMemberService;
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
    public ResponseEntity<List<TeamMemberResponse>> getTeamMembers(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId) {

        List<TeamMemberResponse> teamMembers = teamMemberService.getTeamMembers(teamId, memberId);
        return ResponseEntity.ok(teamMembers);
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> leaveTeam(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId) {

        teamMemberService.leaveTeam(memberId, teamId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{targetMemberId}")
    public ResponseEntity<Void> kickMember(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @PathVariable Long targetMemberId) {

        teamMemberService.kickMember(memberId, teamId, targetMemberId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{targetMemberId}/role")
    public ResponseEntity<TeamMemberResponse> changeRole(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @PathVariable Long targetMemberId,
            @RequestBody @Valid RoleChangeRequest request) {

        TeamMemberResponse updatedMember = teamMemberService.changeRole(memberId, teamId, targetMemberId, request);
        return ResponseEntity.ok(updatedMember);
    }
}
