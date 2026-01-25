package com.writingboard.server.domain.meeting.exception;

import lombok.Getter;

@Getter
public class MeetingException extends RuntimeException {

    private final ErrorCode errorCode;

    public MeetingException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public MeetingException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
