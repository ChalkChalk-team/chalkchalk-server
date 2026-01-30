package com.writingboard.server.domain.team.controller;

import com.writingboard.server.domain.team.dto.request.TeamCreateRequest;
import com.writingboard.server.domain.team.dto.request.TeamUpdateRequest;
import com.writingboard.server.domain.team.dto.response.TeamResponse;
import com.writingboard.server.domain.team.dto.response.TeamSummaryResponse;
import com.writingboard.server.domain.team.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "새로운 팀 생성", description = "새로운 팀을 생성하고 생성자의 권한은 OWNER가 된다.")
    public ResponseEntity<TeamResponse> createTeam(
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid TeamCreateRequest request) {

        TeamResponse team = teamService.createTeam(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(team);
    }

    @GetMapping
    @Operation(summary = "내가 속한 팀 목록 조회", description = "현재 인증된 사용자가 속한 모든 팀의 요약 정보를 조회한다.")
    public ResponseEntity<List<TeamSummaryResponse>> getMyTeams(
            @AuthenticationPrincipal Long memberId) {

        List<TeamSummaryResponse> teams = teamService.getMyTeams(memberId);
        return ResponseEntity.ok(teams);
    }

    @GetMapping("/{teamId}")
    @Operation(summary = "팀 상세 정보 조회")
    public ResponseEntity<TeamResponse> getTeam(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId) {

        TeamResponse team = teamService.getTeam(memberId, teamId);
        return ResponseEntity.ok(team);
    }

    @PatchMapping("/{teamId}")
    @Operation(summary = "팀 정보 수정", description = "팀의 이름, 설명, 프로필 이미지를 수정")
    public ResponseEntity<TeamResponse> updateTeam(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @RequestBody @Valid TeamUpdateRequest request) {

        TeamResponse team = teamService.updateTeam(memberId, teamId, request);
        return ResponseEntity.ok(team);
    }

    @DeleteMapping("/{teamId}")
    @Operation(summary = "팀 삭제", description = "팀을 삭제한다. 팀의 OWNER만 삭제할 수 있다.")
    public ResponseEntity<Void> deleteTeam(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId) {
        teamService.deleteTeam(memberId, teamId);
        return ResponseEntity.noContent().build();
    }
}
