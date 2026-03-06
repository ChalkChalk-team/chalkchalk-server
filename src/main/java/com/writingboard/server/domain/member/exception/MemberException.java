package com.writingboard.server.domain.member.exception;

import lombok.Getter;

@Getter
public class MemberException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String field;

    public MemberException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.field = null;
    }

    public MemberException(ErrorCode errorCode, String field) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.field = field;
    }
}