package com.writingboard.server.domain.meeting.controller;

import com.writingboard.server.domain.meeting.dto.request.ViewingStateRequest;
import com.writingboard.server.domain.meeting.dto.response.ParticipantViewingResponse;
import com.writingboard.server.domain.meeting.service.CanvasFollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Deprecated
@RestController
@RequestMapping("/api/rooms/{roomUuid}/follow")
@RequiredArgsConstructor
@Tag(name = "Follow", description = "페이지 팔로우 API")
public class CanvasFollowController {

    private final CanvasFollowService canvasFollowService;

    @PostMapping("/viewing")
    @Operation(summary = "현재 보고 있는 페이지 업데이트", description = "현재 보고 있는 자료와 페이지를 업데이트하고 다른 참가자에게 브로드캐스트합니다")
    public ResponseEntity<Void> updateViewingState(
            @AuthenticationPrincipal Long memberId,
            @PathVariable String roomUuid,
            @RequestBody @Valid ViewingStateRequest request) {
        canvasFollowService.updateViewingState(memberId, roomUuid, request.getRoomAssetId(), request.getPageIndex());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{targetMemberId}")
    @Operation(summary = "팔로우 시작", description = "특정 참가자를 팔로우하여 같은 페이지를 봅니다")
    public ResponseEntity<Void> startFollow(
            @AuthenticationPrincipal Long memberId,
            @PathVariable String roomUuid,
            @PathVariable Long targetMemberId) {
        canvasFollowService.startFollow(memberId, roomUuid, targetMemberId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    @Operation(summary = "팔로우 종료", description = "팔로우를 종료합니다")
    public ResponseEntity<Void> stopFollow(
            @AuthenticationPrincipal Long memberId,
            @PathVariable String roomUuid) {
        canvasFollowService.stopFollow(memberId, roomUuid);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/participants/viewing")
    @Operation(summary = "참가자별 현재 페이지 조회", description = "모든 참가자가 현재 보고 있는 자료와 페이지를 조회합니다")
    public ResponseEntity<List<ParticipantViewingResponse>> getParticipantsViewing(
            @AuthenticationPrincipal Long memberId,
            @PathVariable String roomUuid) {
        return ResponseEntity.ok(canvasFollowService.getParticipantsViewing(memberId, roomUuid));
    }
}
