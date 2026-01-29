package com.writingboard.server.domain.voice.exception;

import lombok.Getter;

@Getter
public class VoiceException extends RuntimeException {

    private final VoiceErrorCode errorCode;

    public VoiceException(VoiceErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public VoiceException(VoiceErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
