package com.writingboard.server.domain.team.controller;

import com.writingboard.server.domain.team.dto.request.TeamCreateRequest;
import com.writingboard.server.domain.team.dto.request.TeamUpdateRequest;
import com.writingboard.server.domain.team.dto.response.TeamResponse;
import com.writingboard.server.domain.team.dto.response.TeamSummaryResponse;
import com.writingboard.server.domain.team.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping
    public ResponseEntity<TeamResponse> createTeam(
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid TeamCreateRequest request) {

        TeamResponse team = teamService.createTeam(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(team);
    }

    @GetMapping
    public ResponseEntity<List<TeamSummaryResponse>> getMyTeams(
            @AuthenticationPrincipal Long memberId) {

        List<TeamSummaryResponse> teams = teamService.getMyTeams(memberId);
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamResponse> getTeam(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId) {

        TeamResponse team = teamService.getTeam(memberId, teamId);
        return ResponseEntity.ok(team);
    }

    @PatchMapping("/{teamId}")
    public ResponseEntity<TeamResponse> updateTeam(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @RequestBody @Valid TeamUpdateRequest request) {

        TeamResponse team = teamService.updateTeam(memberId, teamId, request);
        return ResponseEntity.ok(team);
    }

    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> deleteTeam(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId) {
        teamService.deleteTeam(memberId, teamId);
        return ResponseEntity.noContent().build();
    }
}
