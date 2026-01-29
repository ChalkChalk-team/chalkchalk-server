package com.writingboard.server.domain.voice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum VoiceErrorCode {

    // Signal
    INVALID_SIGNAL(HttpStatus.BAD_REQUEST, "VOICE_001", "유효하지 않은 시그널입니다"),
    SIGNAL_DATA_TOO_LARGE(HttpStatus.BAD_REQUEST, "VOICE_002", "시그널 데이터가 너무 큽니다 (최대 10000자)"),

    // Room
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "VOICE_003", "회의실을 찾을 수 없습니다"),
    ROOM_CLOSED(HttpStatus.BAD_REQUEST, "VOICE_004", "종료된 회의실에서는 음성 통화를 할 수 없습니다"),

    // Participant
    NOT_PARTICIPANT(HttpStatus.FORBIDDEN, "VOICE_005", "해당 회의실의 참여자가 아닙니다"),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "VOICE_006", "사용자를 찾을 수 없습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
