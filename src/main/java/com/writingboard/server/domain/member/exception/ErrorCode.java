package com.writingboard.server.domain.member.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Member 관련 (MEMBER_XXX)
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_001", "사용자를 찾을 수 없습니다."),
    ALREADY_DELETED(HttpStatus.BAD_REQUEST, "MEMBER_002", "이미 탈퇴한 회원입니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "MEMBER_003", "잘못된 입력값입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "MEMBER_004", "이미 존재하는 이메일입니다."),
    DUPLICATE_USER_ID(HttpStatus.CONFLICT, "MEMBER_005", "이미 사용 중인 사용자 ID입니다."),
    DEVICE_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "MEMBER_006", "유효하지 않은 디바이스 토큰입니다."),
    DEVICE_TOKEN_CONFLICT(HttpStatus.CONFLICT, "MEMBER_007", "다른 사용자에게 등록된 디바이스 토큰입니다."),
    DEVICE_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_008", "디바이스 토큰을 찾을 수 없습니다."),
    DEVICE_TOKEN_NOT_OWNED(HttpStatus.FORBIDDEN, "MEMBER_009", "해당 디바이스 토큰에 대한 권한이 없습니다."),
    DEVICE_TOKEN_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "MEMBER_010", "등록 가능한 디바이스 토큰 수를 초과했습니다."),

    // Auth 관련 (AUTH_XXX) 일단 여기 나중에 옮길거
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_001", "인증이 필요합니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_002", "토큰이 만료되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
