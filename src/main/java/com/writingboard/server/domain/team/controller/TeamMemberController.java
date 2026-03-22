package com.writingboard.server.domain.team.controller;

import com.writingboard.server.domain.team.dto.request.RoleChangeRequest;
import com.writingboard.server.domain.team.dto.request.TeamProfileUpdateRequest;
import com.writingboard.server.domain.team.dto.response.TeamMemberResponse;
import com.writingboard.server.domain.team.dto.response.TeamProfileResponse;
import com.writingboard.server.domain.team.service.TeamMemberService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @GetMapping("/me/profile")
    @Operation(summary = "내 팀 프로필 조회", description = "현재 인증된 사용자의 팀별 프로필을 조회한다.")
    public ResponseEntity<TeamProfileResponse> getMyProfile(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId) {

        return ResponseEntity.ok(teamMemberService.getMyProfile(memberId, teamId));
    }

    @PutMapping("/me/profile")
    @Operation(summary = "팀 프로필 닉네임 수정", description = "팀별 닉네임을 수정한다.")
    public ResponseEntity<TeamProfileResponse> updateMyProfile(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @RequestBody @Valid TeamProfileUpdateRequest request) {

        return ResponseEntity.ok(teamMemberService.updateMyNickname(memberId, teamId, request));
    }

    @PutMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "팀 프로필 이미지 업로드", description = "팀별 프로필 이미지를 업로드한다.")
    public ResponseEntity<TeamProfileResponse> updateMyProfileImage(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @RequestPart("profileImage") MultipartFile profileImage) {

        return ResponseEntity.ok(teamMemberService.updateMyProfileImage(memberId, teamId, profileImage));
    }

    @GetMapping("/{targetMemberId}/profile")
    @Operation(summary = "특정 멤버의 팀 프로필 조회", description = "특정 멤버의 팀별 프로필을 조회한다.")
    public ResponseEntity<TeamProfileResponse> getMemberProfile(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @PathVariable Long targetMemberId) {

        return ResponseEntity.ok(teamMemberService.getMemberProfile(memberId, teamId, targetMemberId));
    }
}
