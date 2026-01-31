package com.writingboard.server.domain.meeting.exception;

import lombok.Getter;

@Getter
public class MeetingException extends RuntimeException {

    private final MeetingErrorCode meetingErrorCode;

    public MeetingException(MeetingErrorCode meetingErrorCode) {
        super(meetingErrorCode.getMessage());
        this.meetingErrorCode = meetingErrorCode;
    }

    public MeetingException(MeetingErrorCode meetingErrorCode, String message) {
        super(message);
        this.meetingErrorCode = meetingErrorCode;
    }
}
