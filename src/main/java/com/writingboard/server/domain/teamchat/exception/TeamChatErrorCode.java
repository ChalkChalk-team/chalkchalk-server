package com.writingboard.server.domain.teamchat.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TeamChatErrorCode {

    EMPTY_MESSAGE(HttpStatus.BAD_REQUEST, "TEAM_CHAT_001", "메시지 내용이 비어있습니다"),
    MESSAGE_TOO_LONG(HttpStatus.BAD_REQUEST, "TEAM_CHAT_002", "메시지가 너무 깁니다 (최대 2000자)"),
    NOT_TEAM_MEMBER(HttpStatus.FORBIDDEN, "TEAM_CHAT_003", "팀 멤버가 아닙니다"),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "TEAM_CHAT_004", "사용자를 찾을 수 없습니다"),
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "TEAM_CHAT_005", "메시지를 찾을 수 없습니다"),
    NOT_MESSAGE_SENDER(HttpStatus.FORBIDDEN, "TEAM_CHAT_006", "본인이 보낸 메시지만 삭제할 수 있습니다"),
    TEAM_MISMATCH(HttpStatus.BAD_REQUEST, "TEAM_CHAT_007", "해당 팀의 메시지가 아닙니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
