package com.writingboard.server.domain.meeting.controller;

import com.writingboard.server.domain.meeting.dto.request.CanvasUpdateRequest;
import com.writingboard.server.domain.meeting.dto.response.CanvasPageResponse;
import com.writingboard.server.domain.meeting.dto.response.CanvasSnapshotResponse;
import com.writingboard.server.domain.meeting.entity.enums.SnapshotTriggerType;
import com.writingboard.server.domain.meeting.service.CanvasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Deprecated
@RestController
@RequestMapping("/api/rooms/{roomUuid}/canvas")
@RequiredArgsConstructor
@Tag(name = "Canvas", description = "필기 데이터 관리 API")
public class CanvasController {

    private final CanvasService canvasService;

    @GetMapping("/{roomAssetId}/pages/{pageIndex}")
    @Operation(summary = "필기 데이터 조회", description = "특정 페이지의 필기 데이터를 조회합니다")
    public ResponseEntity<CanvasPageResponse> getCanvasPage(
            @AuthenticationPrincipal Long memberId,
            @PathVariable String roomUuid,
            @PathVariable Long roomAssetId,
            @PathVariable int pageIndex) {
        return ResponseEntity.ok(canvasService.getCanvasPage(memberId, roomUuid, roomAssetId, pageIndex));
    }

    @PutMapping("/{roomAssetId}/pages/{pageIndex}")
    @Operation(summary = "필기 데이터 저장", description = "특정 페이지의 필기 데이터를 저장합니다")
    public ResponseEntity<CanvasPageResponse> updateCanvasPage(
            @AuthenticationPrincipal Long memberId,
            @PathVariable String roomUuid,
            @PathVariable Long roomAssetId,
            @PathVariable int pageIndex,
            @RequestBody CanvasUpdateRequest request) {
        return ResponseEntity.ok(canvasService.updateCanvasPage(memberId, roomUuid, roomAssetId, pageIndex, request));
    }

    @PostMapping("/{roomAssetId}/pages/{pageIndex}/snapshot")
    @Operation(summary = "스냅샷 생성", description = "현재 필기 데이터의 스냅샷을 생성합니다")
    public ResponseEntity<CanvasSnapshotResponse> createSnapshot(
            @AuthenticationPrincipal Long memberId,
            @PathVariable String roomUuid,
            @PathVariable Long roomAssetId,
            @PathVariable int pageIndex,
            @RequestParam(defaultValue = "MANUAL") SnapshotTriggerType triggerType) {
        return ResponseEntity.ok(canvasService.createSnapshot(memberId, roomUuid, roomAssetId, pageIndex, triggerType));
    }

    @PostMapping("/{roomAssetId}/pages/{pageIndex}/restore")
    @Operation(summary = "스냅샷 복원", description = "가장 최근 스냅샷으로 필기 데이터를 복원합니다")
    public ResponseEntity<CanvasPageResponse> restoreFromSnapshot(
            @AuthenticationPrincipal Long memberId,
            @PathVariable String roomUuid,
            @PathVariable Long roomAssetId,
            @PathVariable int pageIndex) {
        return ResponseEntity.ok(canvasService.restoreFromSnapshot(memberId, roomUuid, roomAssetId, pageIndex));
    }
}
