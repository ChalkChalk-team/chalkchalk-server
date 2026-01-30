package com.writingboard.server.domain.team.exception;

import lombok.Getter;

@Getter
public class TeamException extends RuntimeException {

    private final TeamErrorCode errorCode;

    public TeamException(TeamErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public TeamException(TeamErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
