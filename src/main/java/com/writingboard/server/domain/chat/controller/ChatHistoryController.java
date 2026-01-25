package com.writingboard.server.domain.chat.controller;

import com.writingboard.server.domain.chat.dto.response.ChatHistoryResponse;
import com.writingboard.server.domain.chat.service.ChatHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Tag(name = "Chat", description = "채팅 API")
@RestController
@RequestMapping("/api/rooms/{roomUuid}/messages")
@RequiredArgsConstructor
public class ChatHistoryController {

    private final ChatHistoryService chatHistoryService;

    /**
     * 채팅 기록 조회 (커서 기반 페이징)
     * - 진행 중인 회의실: 현재 참여자만 조회 가능
     * - 종료된 회의실: 참여 이력이 있는 사용자만 조회 가능
     */
    @Operation(summary = "채팅 기록 조회", description = "회의실의 채팅 기록을 커서 기반으로 조회합니다.")
    @GetMapping
    public ResponseEntity<ChatHistoryResponse> getMessages(
            @AuthenticationPrincipal Long memberId,
            @PathVariable String roomUuid,
            @Parameter(description = "이 시각 이전의 메시지를 조회 (ISO-8601 형식)")
            @RequestParam(required = false) Instant before,
            @Parameter(description = "조회할 메시지 수 (기본: 50, 최대: 100)")
            @RequestParam(defaultValue = "50") @Max(100) Integer limit) {

        return ResponseEntity.ok(
                chatHistoryService.getMessages(memberId, roomUuid, before, limit)
        );
    }
}
