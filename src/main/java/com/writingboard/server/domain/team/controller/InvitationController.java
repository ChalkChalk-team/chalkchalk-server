package com.writingboard.server.domain.team.controller;

import com.writingboard.server.domain.team.dto.response.TeamInvitationResponse;
import com.writingboard.server.domain.team.service.TeamInvitationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final TeamInvitationService teamInvitationService;

    @GetMapping
    @Operation(summary = "내 대기 중인 팀 초대 조회", description = "현재 인증된 사용자가 받은 대기 중인 팀 초대 목록을 조회한다.")
    public ResponseEntity<List<TeamInvitationResponse>> getMyPendingInvitations(
            @AuthenticationPrincipal Long memberId) {

        List<TeamInvitationResponse> myPendingInvitations = teamInvitationService.getMyPendingInvitations(memberId);
        return ResponseEntity.ok(myPendingInvitations);
    }

    @PostMapping("/link/{inviteToken}/accept")
    @Operation(summary = "팀 링크 초대 수락", description = "초대 링크 토큰을 사용하여 팀 초대를 수락한다.")
    public ResponseEntity<Void> acceptLinkInvitation(
            @AuthenticationPrincipal Long memberId,
            @PathVariable String inviteToken) {

        teamInvitationService.acceptLinkInvitation(memberId, inviteToken);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{invitationId}/accept")
    @Operation(summary = "직접 초대 수락", description = "토큰이 아닌 어플리케이션 내에서 직접 받은 팀 초대를 수락한다.")
    public ResponseEntity<Void> acceptDirectInvitation(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long invitationId) {

        teamInvitationService.acceptDirectInvitation(memberId, invitationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{invitationId}/reject")
    @Operation(summary = "팀 초대 거절", description = "받은 팀 초대를 거절한다.")
    public ResponseEntity<Void> rejectInvitation(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long invitationId) {

        teamInvitationService.rejectInvitation(memberId, invitationId);
        return ResponseEntity.ok().build();
    }
}
