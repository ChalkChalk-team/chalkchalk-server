package com.writingboard.server.domain.teamchat.controller;

import com.writingboard.server.domain.teamchat.dto.request.TeamChatMessageRequest;
import com.writingboard.server.domain.teamchat.dto.response.TeamChatMessageResponse;
import com.writingboard.server.domain.teamchat.service.TeamChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams/{teamId}/chat/messages")
@RequiredArgsConstructor
@Tag(name = "Team Chat", description = "팀 채팅 API")
public class TeamChatController {

    private final TeamChatService teamChatService;

    @GetMapping
    @Operation(summary = "팀 채팅 메시지 목록 조회")
    public ResponseEntity<Page<TeamChatMessageResponse>> getMessages(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 100),
                Sort.by(Sort.Direction.DESC, "timestamp"));
        return ResponseEntity.ok(teamChatService.getMessages(memberId, teamId, pageable));
    }

    @PostMapping
    @Operation(summary = "팀 채팅 메시지 전송")
    public ResponseEntity<TeamChatMessageResponse> sendMessage(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @RequestBody @Valid TeamChatMessageRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(teamChatService.sendMessage(memberId, teamId, request));
    }

    @DeleteMapping("/{messageId}")
    @Operation(summary = "팀 채팅 메시지 삭제")
    public ResponseEntity<Void> deleteMessage(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long teamId,
            @PathVariable String messageId) {

        teamChatService.deleteMessage(memberId, teamId, messageId);
        return ResponseEntity.noContent().build();
    }
}
