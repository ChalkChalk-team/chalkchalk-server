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
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(teamService.createTeam(memberId, request));
    }

    @GetMapping
    public ResponseEntity<List<TeamSummaryResponse>> getMyTeams(
            @AuthenticationPrincipal Long memberId) {
        return ResponseEntity.ok(teamService.getMyTeams(memberId));
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamResponse> getTeam(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId) {
        return ResponseEntity.ok(teamService.getTeam(memberId, teamId));
    }

    @PatchMapping("/{teamId}")
    public ResponseEntity<TeamResponse> updateTeam(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @RequestBody @Valid TeamUpdateRequest request) {
        return ResponseEntity.ok(teamService.updateTeam(memberId, teamId, request));
    }

    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> deleteTeam(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId) {
        teamService.deleteTeam(memberId, teamId);
        return ResponseEntity.noContent().build();
    }
}
