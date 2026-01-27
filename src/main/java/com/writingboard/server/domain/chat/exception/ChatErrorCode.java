package com.writingboard.server.domain.chat.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ChatErrorCode {

    // Message
    EMPTY_MESSAGE(HttpStatus.BAD_REQUEST, "CHAT_001", "메시지 내용이 비어있습니다"),
    MESSAGE_TOO_LONG(HttpStatus.BAD_REQUEST, "CHAT_002", "메시지가 너무 깁니다 (최대 2000자)"),

    // Room
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_003", "회의실을 찾을 수 없습니다"),
    ROOM_CLOSED(HttpStatus.BAD_REQUEST, "CHAT_004", "종료된 회의실에서는 채팅할 수 없습니다"),

    // Participant
    NOT_PARTICIPANT(HttpStatus.FORBIDDEN, "CHAT_005", "해당 회의실의 참여자가 아닙니다"),
    NOT_PARTICIPANT_HISTORY(HttpStatus.FORBIDDEN, "CHAT_006", "채팅 기록을 조회할 권한이 없습니다"),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_007", "사용자를 찾을 수 없습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
