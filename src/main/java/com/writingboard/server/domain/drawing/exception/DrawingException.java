package com.writingboard.server.domain.drawing.exception;

import lombok.Getter;


@Getter
public class DrawingException extends RuntimeException {

    private final DrawingErrorCode errorCode;

    public DrawingException(DrawingErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public DrawingException(DrawingErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
