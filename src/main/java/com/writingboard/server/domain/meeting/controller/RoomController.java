package com.writingboard.server.domain.meeting.controller;

import com.writingboard.server.domain.meeting.dto.request.RoomCreateRequest;
import com.writingboard.server.domain.meeting.dto.request.RoomJoinRequest;
import com.writingboard.server.domain.meeting.dto.response.*;
import com.writingboard.server.domain.meeting.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
@Slf4j
public class RoomController {

    private final RoomService roomService;

    /**
     * 회의실 생성
     */
    @PostMapping
    public ResponseEntity<RoomCreateResponse> createRoom(
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid RoomCreateRequest request) {

        RoomCreateResponse createdRoom = roomService.createRoom(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom);
    }

    /**
     * 회의실 종료 (호스트만)
     */
    @DeleteMapping("/{roomUuid}")
    public ResponseEntity<Void> closeRoom(
            @AuthenticationPrincipal Long memberId,
            @PathVariable String roomUuid) {
        roomService.closeRoom(memberId, roomUuid);
        return ResponseEntity.noContent().build();
    }

    /**
     * 회의실 상세 조회
     */
    @GetMapping("/{roomUuid}")
    public ResponseEntity<RoomDetailResponse> getRoom(
            @PathVariable String roomUuid) {

        RoomDetailResponse roomDetail = roomService.getRoom(roomUuid);
        return ResponseEntity.ok(roomDetail);
    }

    /**
     * 내가 참여중인 회의실 목록
     */
    @GetMapping("/me")
    public ResponseEntity<List<RoomSummaryResponse>> getMyRooms(
            @AuthenticationPrincipal Long memberId) {
//        log.info("회원ID : {}", memberId);
        return ResponseEntity.ok(roomService.getMyRooms(memberId));
    }

    /**
     * 내가 호스트인 회의실 목록 (히스토리 포함)
     */
    @GetMapping("/hosted")
    public ResponseEntity<Page<RoomSummaryResponse>> getMyHostedRooms(
            @AuthenticationPrincipal Long memberId,
            Pageable pageable) {
        return ResponseEntity.ok(roomService.getMyHostedRooms(memberId, pageable));
    }

    /**
     * 회의실 입장
     */
    @PostMapping("/{roomUuid}/join")
    public ResponseEntity<RoomJoinResponse> joinRoom(
            @AuthenticationPrincipal Long memberId,
            @PathVariable String roomUuid,
            @RequestBody(required = false) RoomJoinRequest request) {

        RoomJoinResponse joined = roomService.joinRoom(memberId, roomUuid, request);
        return ResponseEntity.ok(joined);
    }

    /**
     * 초대 토큰으로 회의실 입장
     */
    @PostMapping("/join/invite/{inviteToken}")
    public ResponseEntity<RoomJoinResponse> joinRoomByInvite(
            @AuthenticationPrincipal Long memberId,
            @PathVariable String inviteToken,
            @RequestBody(required = false) RoomJoinRequest request) {
        String password = request != null ? request.getPassword() : null;
        return ResponseEntity.ok(roomService.joinRoomByInvite(memberId, inviteToken, password));
    }

    /**
     * 회의실 퇴장
     */
    @PostMapping("/{roomUuid}/leave")
    public ResponseEntity<Void> leaveRoom(
            @AuthenticationPrincipal Long memberId,
            @PathVariable String roomUuid) {
        roomService.leaveRoom(memberId, roomUuid);
        return ResponseEntity.noContent().build();
    }

    /**
     * 참여자 강제 퇴장
     */
    @PostMapping("/{roomUuid}/kick/{targetMemberId}")
    public ResponseEntity<Void> kickParticipant(
            @AuthenticationPrincipal Long memberId,
            @PathVariable String roomUuid,
            @PathVariable Long targetMemberId) {
        roomService.kickParticipant(memberId, roomUuid, targetMemberId);
        return ResponseEntity.noContent().build();
    }
}
