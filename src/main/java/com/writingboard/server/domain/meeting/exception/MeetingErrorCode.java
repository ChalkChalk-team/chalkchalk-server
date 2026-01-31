package com.writingboard.server.domain.meeting.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MeetingErrorCode {
    // Team
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "TEAM_001", "팀을 찾을) 수 없습니다"),
    NOT_TEAM_MEMBER(HttpStatus.FORBIDDEN, "TEAM_002", "팀 멤버가 아닙니다"),

    // Room
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "ROOM_001", "회의실을 찾을 수 없습니다"),
    ROOM_CLOSED(HttpStatus.BAD_REQUEST, "ROOM_002", "종료된 회의실입니다"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "ROOM_003", "비밀번호가 일치하지 않습니다"),

    // Participant
    NOT_PARTICIPANT(HttpStatus.FORBIDDEN, "PARTICIPANT_001", "회의실 참여자가 아닙니다"),
    ALREADY_JOINED(HttpStatus.BAD_REQUEST, "PARTICIPANT_002", "이미 참여중인 회의실입니다"),
    CANNOT_KICK_HOST(HttpStatus.BAD_REQUEST, "PARTICIPANT_003", "호스트는 강퇴할 수 없습니다"),

    // Invite
    INVITE_NOT_FOUND(HttpStatus.NOT_FOUND, "INVITE_001", "초대를 찾을 수 없습니다"),
    INVITE_EXPIRED(HttpStatus.BAD_REQUEST, "INVITE_002", "만료된 초대입니다"),

    // Auth
    FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH_001", "권한이 없습니다"),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_001", "사용자를 찾을 수 없습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
