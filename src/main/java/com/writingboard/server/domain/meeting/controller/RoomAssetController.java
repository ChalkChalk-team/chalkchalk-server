package com.writingboard.server.domain.meeting.controller;

import com.writingboard.server.domain.meeting.dto.request.LoadPersonalAssetRequest;
import com.writingboard.server.domain.meeting.dto.request.LoadTeamAssetRequest;
import com.writingboard.server.domain.meeting.dto.request.PageChangeRequest;
import com.writingboard.server.domain.meeting.dto.response.RoomAssetResponse;
import com.writingboard.server.domain.meeting.service.RoomAssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms/{roomUuid}/assets")
@RequiredArgsConstructor
@Tag(name = "Room Asset", description = "회의실 자료 관리 API")
public class RoomAssetController {

    private final RoomAssetService roomAssetService;

    @PostMapping("/team")
    @Operation(summary = "팀 자료 불러오기", description = "팀 자료를 회의실에 불러옵니다")
    public ResponseEntity<RoomAssetResponse> loadTeamAsset(
            @AuthenticationPrincipal Long memberId,
            @PathVariable String roomUuid,
            @RequestBody @Valid LoadTeamAssetRequest request) {
        RoomAssetResponse response = roomAssetService.loadTeamAsset(memberId, roomUuid, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/personal")
    @Operation(summary = "개인 자료 불러오기", description = "개인 자료를 팀 자료로 복사 후 회의실에 불러옵니다")
    public ResponseEntity<RoomAssetResponse> loadPersonalAsset(
            @AuthenticationPrincipal Long memberId,
            @PathVariable String roomUuid,
            @RequestBody @Valid LoadPersonalAssetRequest request) {
        RoomAssetResponse response = roomAssetService.loadPersonalAsset(memberId, roomUuid, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "회의실 자료 목록 조회", description = "회의실에 불러온 자료 목록을 조회합니다")
    public ResponseEntity<List<RoomAssetResponse>> getRoomAssets(
            @AuthenticationPrincipal Long memberId,
            @PathVariable String roomUuid) {
        return ResponseEntity.ok(roomAssetService.getRoomAssets(memberId, roomUuid));
    }

    @PostMapping("/{roomAssetId}/activate")
    @Operation(summary = "자료 활성화", description = "특정 자료를 화면에 활성화합니다")
    public ResponseEntity<RoomAssetResponse> activateAsset(
            @AuthenticationPrincipal Long memberId,
            @PathVariable String roomUuid,
            @PathVariable Long roomAssetId) {
        return ResponseEntity.ok(roomAssetService.activateAsset(memberId, roomUuid, roomAssetId));
    }

    @PostMapping("/{roomAssetId}/page")
    @Operation(summary = "페이지 변경", description = "활성화된 자료의 페이지를 변경합니다")
    public ResponseEntity<RoomAssetResponse> changePage(
            @AuthenticationPrincipal Long memberId,
            @PathVariable String roomUuid,
            @PathVariable Long roomAssetId,
            @RequestBody @Valid PageChangeRequest request) {
        return ResponseEntity.ok(roomAssetService.changePage(memberId, roomUuid, roomAssetId, request));
    }
}
