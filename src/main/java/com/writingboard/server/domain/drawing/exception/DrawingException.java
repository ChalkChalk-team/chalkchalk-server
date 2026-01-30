package com.writingboard.server.domain.drawing.exception;

import lombok.Getter;

/**
 * 드로잉 도메인 커스텀 예외
 */
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
