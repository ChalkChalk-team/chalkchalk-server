package com.writingboard.server.global.exception;

import com.writingboard.server.domain.auth.exception.AuthException;
import com.writingboard.server.domain.chat.exception.ChatException;
import com.writingboard.server.domain.meeting.exception.MeetingException;
import com.writingboard.server.domain.member.exception.MemberException;
import com.writingboard.server.domain.team.exception.TeamException;
import com.writingboard.server.domain.voice.exception.VoiceException;
import com.writingboard.server.global.common.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(AuthException e) {
        log.warn("AuthException: {}", e.getMessage());
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ErrorResponse.of(e.getErrorCode().getCode(), e.getMessage()));
    }

    @ExceptionHandler(MeetingException.class)
    public ResponseEntity<ErrorResponse> handleMeetingException(MeetingException e) {
        log.warn("MeetingException: {}", e.getMessage());
        return ResponseEntity
                .status(e.getMeetingErrorCode().getStatus())
                .body(ErrorResponse.of(e.getMeetingErrorCode().getCode(), e.getMessage()));
    }

    @ExceptionHandler(ChatException.class)
    public ResponseEntity<ErrorResponse> handleChatException(ChatException e) {
        log.warn("ChatException: {}", e.getMessage());
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ErrorResponse.of(e.getErrorCode().getCode(), e.getMessage()));
    }

    @ExceptionHandler(VoiceException.class)
    public ResponseEntity<ErrorResponse> handleVoiceException(VoiceException e) {
        log.warn("VoiceException: {}", e.getMessage());
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ErrorResponse.of(e.getErrorCode().getCode(), e.getMessage()));
    }

    @ExceptionHandler(MemberException.class)
    public ResponseEntity<ErrorResponse> handleMemberException(MemberException e) {
        log.warn("MemberException: {}", e.getMessage());
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ErrorResponse.of(e.getErrorCode().getCode(), e.getMessage()));
    }

    @ExceptionHandler(TeamException.class)
    public ResponseEntity<ErrorResponse> handleTeamException(TeamException e) {
        log.warn("TeamException: {}", e.getMessage());
        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(ErrorResponse.of(e.getErrorCode().getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .findFirst()
                .orElse("유효성 검증 실패");
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("VALIDATION_ERROR", message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity.internalServerError()
                .body(ErrorResponse.of("INTERNAL_ERROR", "서버 오류가 발생했습니다"));
    }
}
