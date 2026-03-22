package com.writingboard.server.domain.teamchat.exception;

import lombok.Getter;

@Getter
public class TeamChatException extends RuntimeException {

    private final TeamChatErrorCode errorCode;

    public TeamChatException(TeamChatErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
