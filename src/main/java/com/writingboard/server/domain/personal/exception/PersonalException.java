package com.writingboard.server.domain.personal.exception;

import lombok.Getter;

@Getter
public class PersonalException extends RuntimeException {

    private final PersonalErrorCode errorCode;

    public PersonalException(PersonalErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public PersonalException(PersonalErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
}
