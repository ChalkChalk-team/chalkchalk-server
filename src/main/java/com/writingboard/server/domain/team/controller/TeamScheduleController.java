package com.writingboard.server.domain.team.controller;

import com.writingboard.server.domain.team.dto.request.ScheduleCreateRequest;
import com.writingboard.server.domain.team.dto.request.ScheduleUpdateRequest;
import com.writingboard.server.domain.team.dto.response.ScheduleResponse;
import com.writingboard.server.domain.team.service.TeamScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/teams/{teamId}/schedules")
@RequiredArgsConstructor
@Tag(name = "Team Schedule", description = "팀 일정 API")
public class TeamScheduleController {

    private final TeamScheduleService teamScheduleService;

    @GetMapping
    @Operation(summary = "기간별 일정 목록 조회")
    public ResponseEntity<List<ScheduleResponse>> getSchedules(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @RequestParam OffsetDateTime from,
            @RequestParam OffsetDateTime to) {

        return ResponseEntity.ok(teamScheduleService.getSchedules(memberId, teamId, from, to));
    }

    @PostMapping
    @Operation(summary = "일정 생성")
    public ResponseEntity<ScheduleResponse> createSchedule(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @RequestBody @Valid ScheduleCreateRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(teamScheduleService.createSchedule(memberId, teamId, request));
    }

    @PutMapping("/{scheduleId}")
    @Operation(summary = "일정 수정")
    public ResponseEntity<ScheduleResponse> updateSchedule(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @PathVariable Long scheduleId,
            @RequestBody @Valid ScheduleUpdateRequest request) {

        return ResponseEntity.ok(teamScheduleService.updateSchedule(memberId, teamId, scheduleId, request));
    }

    @DeleteMapping("/{scheduleId}")
    @Operation(summary = "일정 삭제")
    public ResponseEntity<Void> deleteSchedule(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @PathVariable Long scheduleId) {

        teamScheduleService.deleteSchedule(memberId, teamId, scheduleId);
        return ResponseEntity.noContent().build();
    }
}
