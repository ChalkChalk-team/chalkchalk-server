package com.writingboard.server.domain.drawing.controller;

import com.writingboard.server.domain.drawing.document.DrawingSnapshot;
import com.writingboard.server.domain.drawing.dto.request.DrawingSnapshotRequest;
import com.writingboard.server.domain.drawing.dto.response.DrawingSnapshotResponse;
import com.writingboard.server.domain.drawing.dto.response.DrawingStrokeDto;
import com.writingboard.server.domain.drawing.enums.DrawingMessageType;
import com.writingboard.server.domain.drawing.exception.DrawingErrorCode;
import com.writingboard.server.domain.drawing.exception.DrawingException;
import com.writingboard.server.domain.drawing.service.DrawingRedisPublisher;
import com.writingboard.server.domain.drawing.service.DrawingSnapshotService;
import com.writingboard.server.domain.meeting.entity.Room;
import com.writingboard.server.domain.meeting.entity.RoomParticipant;
import com.writingboard.server.domain.meeting.entity.enums.ParticipantRole;
import com.writingboard.server.domain.meeting.entity.enums.ParticipantState;
import com.writingboard.server.domain.meeting.entity.enums.RoomStatus;
import com.writingboard.server.domain.meeting.repository.RoomParticipantRepository;
import com.writingboard.server.domain.meeting.repository.RoomRepository;
import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.member.repository.MemberRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 드로잉 스냅샷 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/rooms/{roomUuid}/assets/{roomAssetId}/snapshots")
@RequiredArgsConstructor
public class DrawingSnapshotController {

    private final DrawingSnapshotService snapshotService;
    private final RoomRepository roomRepository;
    private final RoomParticipantRepository participantRepository;
    private final MemberRepository memberRepository;
    private final DrawingRedisPublisher drawingRedisPublisher;

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

        Room room = roomRepository.findByRoomUuid(roomUuid)
                .orElseThrow(() -> new DrawingException(DrawingErrorCode.ROOM_NOT_FOUND));

        if (room.getStatus() != RoomStatus.OPEN) {
            throw new DrawingException(DrawingErrorCode.ROOM_CLOSED);
        }

        RoomParticipant participant = participantRepository
                .findByRoomIdAndMemberId(room.getId(), memberId)
                .orElseThrow(() -> new DrawingException(DrawingErrorCode.NOT_PARTICIPANT));

        if (participant.getState() != ParticipantState.JOINED) {
            throw new DrawingException(DrawingErrorCode.NOT_PARTICIPANT);
        }

        if (participant.getRole() != ParticipantRole.HOST) {
            throw new DrawingException(DrawingErrorCode.SNAPSHOT_PERMISSION_DENIED);
        }

        DrawingSnapshot snapshot = snapshotService.saveSnapshot(
                memberId,
                roomUuid,
                roomAssetId,
                request.getPageIndex(),
                request.getLastIncludedVersion(),
                request.getSnapshotData()
        );

        broadcastSnapshotNotification(snapshot);

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

    /**
     * 스냅샷 생성 알림 브로드캐스트
     */
    private void broadcastSnapshotNotification(DrawingSnapshot snapshot) {
        try {
            Member creator = memberRepository.findById(snapshot.getCreatedBy())
                    .orElseThrow(() -> new DrawingException(DrawingErrorCode.MEMBER_NOT_FOUND));

            DrawingStrokeDto notificationDto = DrawingStrokeDto.of(
                    snapshot.getRoomUuid(),
                    snapshot.getRoomAssetId(),
                    creator.getId(),
                    creator.getName(),
                    DrawingMessageType.SNAPSHOT,
                    snapshot.getPageIndex(),
                    null,
                    null,
                    snapshot.getVersion()
            );

            drawingRedisPublisher.publish(snapshot.getRoomUuid(), notificationDto);

            log.debug("스냅샷 생성 알림 브로드캐스트: roomUuid={}, roomAssetId={}, version={}",
                    snapshot.getRoomUuid(), snapshot.getRoomAssetId(), snapshot.getVersion());

        } catch (Exception e) {
            log.error("스냅샷 알림 브로드캐스트 실패", e);
        }
    }
}
