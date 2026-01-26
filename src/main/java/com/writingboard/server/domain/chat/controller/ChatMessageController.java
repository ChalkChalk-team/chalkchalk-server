package com.writingboard.server.domain.chat.controller;

import com.writingboard.server.domain.chat.dto.request.ChatMessageRequest;
import com.writingboard.server.domain.chat.dto.response.ChatMessageDto;
import com.writingboard.server.domain.chat.service.ChatService;
import com.writingboard.server.global.websocket.StompPrincipal;
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
public class ChatMessageController {

    private final ChatService chatService;

    /**
     * 채팅 메시지 전송
     * Client 전송 -> /app/room/{roomUuid}/chat
     * Redis Pub/Sub -> /topic/room/{roomUuid}
     */
    @MessageMapping("/room/{roomUuid}/chat")
    public void handleChatMessage(
            @DestinationVariable String roomUuid,
            @Payload ChatMessageRequest request,
            Principal principal) {

        Long memberId = getMemberId(principal);
        log.debug("채팅 메시지 수신: roomUuid={}, memberId={}", roomUuid, memberId);

        chatService.sendMessage(memberId, roomUuid, request.getContent());
    }

    /**
     * 에러 발생 시 개인 큐로 에러 메시지 전송
     */
    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleError(Exception e) {
        log.error("채팅 메시지 처리 중 에러", e);
        return e.getMessage();
    }

    private Long getMemberId(Principal principal) {
        if (principal instanceof StompPrincipal stompPrincipal) {
            return stompPrincipal.getMemberId();
        }
        throw new IllegalStateException("인증되지 않은 사용자입니다.");
    }
}
