package com.writingboard.server.domain.auth.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode {

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_001", "인증이 필요합니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH_002", "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "유효하지 않은 토큰입니다."),
    SOCIAL_LOGIN_FAILED(HttpStatus.BAD_REQUEST, "AUTH_004", "소셜 로그인에 실패했습니다."),
    INVALID_ID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_005", "유효하지 않은 ID 토큰입니다."),
    UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH_006", "지원하지 않는 로그인 방식입니다."),
    REGISTRATION_REQUIRED(HttpStatus.UNPROCESSABLE_ENTITY, "AUTH_007", "회원가입이 필요합니다. userId와 nickname을 입력해주세요.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}