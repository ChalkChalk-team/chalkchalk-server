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
    USER_ID_DUPLICATE(HttpStatus.CONFLICT, "USER_ID_DUPLICATE", "이미 사용 중인 아이디입니다."),
    USER_ID_INVALID_FORMAT(HttpStatus.BAD_REQUEST, "USER_ID_INVALID_FORMAT", "사용자 ID는 영문, 숫자, 밑줄(_)만 허용하며 3~20자여야 합니다."),
    NICKNAME_INVALID(HttpStatus.BAD_REQUEST, "NICKNAME_INVALID", "닉네임은 30자 이내여야 합니다."),

    // Auth 관련 (AUTH_XXX) 일단 여기 나중에 옮길거
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_001", "인증이 필요합니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_002", "토큰이 만료되었습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}