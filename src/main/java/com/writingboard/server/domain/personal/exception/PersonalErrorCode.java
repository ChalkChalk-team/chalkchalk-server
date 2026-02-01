package com.writingboard.server.domain.personal.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PersonalErrorCode {
    ASSET_NOT_FOUND(HttpStatus.NOT_FOUND, "PERSONAL_001", "개인 자료를 찾을 수 없습니다"),
    ASSET_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PERSONAL_002", "본인의 자료만 접근할 수 있습니다");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
