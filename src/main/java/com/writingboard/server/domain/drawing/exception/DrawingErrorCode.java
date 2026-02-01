package com.writingboard.server.domain.drawing.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


@Getter
@RequiredArgsConstructor
public enum DrawingErrorCode {

    // Message Type Validation
    INVALID_MESSAGE_TYPE(HttpStatus.BAD_REQUEST, "DRAWING_001", "WebSocket으로는 ADD, REMOVE만 가능합니다 (SNAPSHOT은 REST API 사용)"),

    // Stroke Data Validation
    INVALID_STROKE_DATA(HttpStatus.BAD_REQUEST, "DRAWING_002", "유효하지 않은 스트로크 데이터입니다"),
    STROKE_DATA_TOO_LARGE(HttpStatus.BAD_REQUEST, "DRAWING_003", "스트로크 데이터가 너무 큽니다 (최대 100000자)"),
    STROKE_ID_REQUIRED(HttpStatus.BAD_REQUEST, "DRAWING_004", "ADD 또는 REMOVE 타입은 strokeId가 필수입니다"),
    STROKE_DATA_REQUIRED(HttpStatus.BAD_REQUEST, "DRAWING_005", "ADD 타입은 strokeData가 필수입니다"),
    INVALID_PAGE_INDEX(HttpStatus.BAD_REQUEST, "DRAWING_006", "유효하지 않은 페이지 인덱스입니다"),

    // Room
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "DRAWING_007", "회의실을 찾을 수 없습니다"),
    ROOM_CLOSED(HttpStatus.BAD_REQUEST, "DRAWING_008", "종료된 회의실에서는 드로잉을 사용할 수 없습니다"),

    // Participant
    NOT_PARTICIPANT(HttpStatus.FORBIDDEN, "DRAWING_009", "해당 회의실의 참여자가 아닙니다"),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "DRAWING_010", "사용자를 찾을 수 없습니다"),

    // Redis
    REDIS_OPERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "DRAWING_011", "Redis 작업 중 오류가 발생했습니다"),

    // Snapshot
    SNAPSHOT_NOT_FOUND(HttpStatus.NOT_FOUND, "DRAWING_012", "스냅샷을 찾을 수 없습니다"),
    SNAPSHOT_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "DRAWING_013", "스냅샷 저장에 실패했습니다"),
    SNAPSHOT_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "DRAWING_014", "스냅샷 생성 권한이 없습니다 (HOST만 가능)");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
