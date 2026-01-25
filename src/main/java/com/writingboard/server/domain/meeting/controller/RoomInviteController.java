package com.writingboard.server.domain.meeting.controller;

import com.writingboard.server.domain.meeting.dto.request.InviteLinkRequest;
import com.writingboard.server.domain.meeting.dto.response.InviteLinkResponse;
import com.writingboard.server.domain.meeting.service.RoomInviteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms/{roomUuid}/invites")
@RequiredArgsConstructor
public class RoomInviteController {

    private final RoomInviteService inviteService;

    /**
     * 초대 링크 생성
     */
    @PostMapping("/link")
    public ResponseEntity<InviteLinkResponse> createLinkInvite(
            @AuthenticationPrincipal Long memberId,
            @PathVariable String roomUuid,
            @RequestBody(required = false) InviteLinkRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inviteService.createLinkInvite(memberId, roomUuid, request));
    }
}
