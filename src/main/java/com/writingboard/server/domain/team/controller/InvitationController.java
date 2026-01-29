package com.writingboard.server.domain.team.controller;

import com.writingboard.server.domain.team.dto.response.TeamInvitationResponse;
import com.writingboard.server.domain.team.service.TeamInvitationService;
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
    public ResponseEntity<List<TeamInvitationResponse>> getMyPendingInvitations(
            @AuthenticationPrincipal Long memberId) {
        return ResponseEntity.ok(teamInvitationService.getMyPendingInvitations(memberId));
    }

    @PostMapping("/link/{inviteToken}/accept")
    public ResponseEntity<Void> acceptLinkInvitation(
            @AuthenticationPrincipal Long memberId,
            @PathVariable String inviteToken) {
        teamInvitationService.acceptLinkInvitation(memberId, inviteToken);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{invitationId}/accept")
    public ResponseEntity<Void> acceptDirectInvitation(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long invitationId) {
        teamInvitationService.acceptDirectInvitation(memberId, invitationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{invitationId}/reject")
    public ResponseEntity<Void> rejectInvitation(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long invitationId) {
        teamInvitationService.rejectInvitation(memberId, invitationId);
        return ResponseEntity.ok().build();
    }
}
