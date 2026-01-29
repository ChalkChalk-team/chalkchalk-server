package com.writingboard.server.domain.voice.controller;

import com.writingboard.server.domain.voice.dto.request.VoiceSignalRequest;
import com.writingboard.server.domain.voice.service.VoiceService;
import com.writingboard.server.global.websocket.StompPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class VoiceSignalingController {

    private final VoiceService voiceService;

    /**
     * WebRTC 시그널 전송
     * Client 전송 -> /app/room/{roomUuid}/voice
     * Redis Pub/Sub -> /topic/room/{roomUuid}/voice
     */
    @MessageMapping("/room/{roomUuid}/voice")
    public void handleVoiceSignal(
            @DestinationVariable String roomUuid,
            @Payload @Valid VoiceSignalRequest request,
            Principal principal) {

        Long memberId = getMemberId(principal);
        voiceService.sendSignal(memberId, roomUuid, request);
    }

    /**
     * 에러 발생 시 개인 큐로 에러 메시지 전송
     */
    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleError(Exception e) {
        log.error("음성 시그널 처리 중 에러", e);
        return e.getMessage();
    }

    private Long getMemberId(Principal principal) {
        if (principal instanceof StompPrincipal stompPrincipal) {
            return stompPrincipal.getMemberId();
        }
        throw new IllegalStateException("인증되지 않은 사용자입니다.");
    }
}
