package com.writingboard.server.domain.drawing.controller;

import com.writingboard.server.domain.drawing.dto.response.DrawingStrokeDto;
import com.writingboard.server.domain.drawing.service.DrawingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 드로잉 히스토리 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class DrawingHistoryController {

    private final DrawingService drawingService;

    /**
     * 특정 RoomAsset 페이지의 스트로크 히스토리 조회
     */
    @GetMapping("/{roomUuid}/assets/{roomAssetId}/strokes")
    public ResponseEntity<List<DrawingStrokeDto>> getStrokeHistory(
            @PathVariable String roomUuid,
            @PathVariable Long roomAssetId,
            @RequestParam Integer pageIndex) {

        log.debug("스트로크 히스토리 조회 요청: roomUuid={}, roomAssetId={}, pageIndex={}",
                roomUuid, roomAssetId, pageIndex);

        List<DrawingStrokeDto> strokes = drawingService.getStrokeHistory(roomUuid, roomAssetId, pageIndex);
        return ResponseEntity.ok(strokes);
    }
}
