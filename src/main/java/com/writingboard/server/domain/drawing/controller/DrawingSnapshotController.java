package com.writingboard.server.domain.drawing.controller;

import com.writingboard.server.domain.drawing.document.DrawingSnapshot;
import com.writingboard.server.domain.drawing.dto.request.DrawingSnapshotRequest;
import com.writingboard.server.domain.drawing.dto.response.DrawingSnapshotResponse;
import com.writingboard.server.domain.drawing.exception.DrawingErrorCode;
import com.writingboard.server.domain.drawing.exception.DrawingException;
import com.writingboard.server.domain.drawing.service.DrawingSnapshotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/rooms/{roomUuid}/assets/{roomAssetId}/snapshots")
@RequiredArgsConstructor
public class DrawingSnapshotController {

    private final DrawingSnapshotService snapshotService;

    /**
     * 스냅샷 생성 (HOST만 가능)
     */
    @PostMapping
    public ResponseEntity<DrawingSnapshotResponse> createSnapshot(
            @AuthenticationPrincipal Long memberId,
            @PathVariable String roomUuid,
            @PathVariable Long roomAssetId,
            @RequestBody @Valid DrawingSnapshotRequest request) {

        log.debug("스냅샷 생성 요청: roomUuid={}, roomAssetId={}, memberId={}, pageIndex={}, version={}",
                roomUuid, roomAssetId, memberId, request.getPageIndex(), request.getLastIncludedVersion());

        DrawingSnapshot snapshot = snapshotService.saveSnapshot(
                memberId,
                roomUuid,
                roomAssetId,
                request.getPageIndex(),
                request.getLastIncludedVersion(),
                request.getSnapshotData()
        );

        DrawingSnapshotResponse response = DrawingSnapshotResponse.from(snapshot);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 특정 RoomAsset 페이지의 최신 스냅샷 조회
     */
    @GetMapping("/latest")
    public ResponseEntity<DrawingSnapshotResponse> getLatestSnapshot(
            @PathVariable String roomUuid,
            @PathVariable Long roomAssetId,
            @RequestParam Integer pageIndex) {

        log.debug("최신 스냅샷 조회 요청: roomUuid={}, roomAssetId={}, pageIndex={}",
                roomUuid, roomAssetId, pageIndex);

        DrawingSnapshot snapshot = snapshotService.getLatestSnapshot(roomAssetId, pageIndex)
                .orElseThrow(() -> new DrawingException(DrawingErrorCode.SNAPSHOT_NOT_FOUND));

        return ResponseEntity.ok(DrawingSnapshotResponse.from(snapshot));
    }

}
