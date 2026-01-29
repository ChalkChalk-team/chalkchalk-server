package com.writingboard.server.domain.team.controller;

import com.writingboard.server.domain.team.dto.request.DirectInviteRequest;
import com.writingboard.server.domain.team.dto.request.TeamInviteLinkRequest;
import com.writingboard.server.domain.team.dto.response.TeamInvitationResponse;
import com.writingboard.server.domain.team.dto.response.TeamInviteLinkResponse;
import com.writingboard.server.domain.team.service.TeamInvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams/{teamId}/invitations")
@RequiredArgsConstructor
public class TeamInvitationController {

    private final TeamInvitationService teamInvitationService;

    @PostMapping("/link")
    public ResponseEntity<TeamInviteLinkResponse> createLinkInvitation(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @RequestBody(required = false) @Valid TeamInviteLinkRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(teamInvitationService.createLinkInvitation(memberId, teamId, request));
    }

    @PostMapping("/direct")
    public ResponseEntity<TeamInvitationResponse> createDirectInvitation(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @RequestBody @Valid DirectInviteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(teamInvitationService.createDirectInvitation(memberId, teamId, request));
    }

    @GetMapping
    public ResponseEntity<List<TeamInvitationResponse>> getTeamInvitations(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId) {
        return ResponseEntity.ok(teamInvitationService.getTeamInvitations(memberId, teamId));
    }
}
