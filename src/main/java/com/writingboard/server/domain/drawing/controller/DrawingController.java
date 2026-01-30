package com.writingboard.server.domain.drawing.controller;

import com.writingboard.server.domain.drawing.dto.request.DrawingStrokeRequest;
import com.writingboard.server.domain.drawing.service.DrawingService;
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

/**
 * 드로잉 WebSocket 컨트롤러
 * Client 전송 -> /app/room/{roomUuid}/drawing
 * Redis Pub/Sub -> /topic/room/{roomUuid}/drawing
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class DrawingController {

    private final DrawingService drawingService;

    /**
     * 드로잉 스트로크 메시지 처리
     */
    @MessageMapping("/room/{roomUuid}/drawing")
    public void handleDrawingStroke(
            @DestinationVariable String roomUuid,
            @Payload DrawingStrokeRequest request,
            Principal principal) {

        Long memberId = getMemberId(principal);
        log.debug("드로잉 스트로크 수신: roomUuid={}, memberId={}, type={}, pageIndex={}",
                roomUuid, memberId, request.getType(), request.getPageIndex());

        drawingService.sendStroke(memberId, roomUuid, request);
    }

    /**
     * WebSocket 메시지 처리 중 에러 핸들링
     */
    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleError(Exception e) {
        log.error("드로잉 메시지 처리 중 에러", e);
        return e.getMessage();
    }

    /**
     * Principal에서 memberId 추출
     */
    private Long getMemberId(Principal principal) {
        if (principal instanceof StompPrincipal stompPrincipal) {
            return stompPrincipal.getMemberId();
        }
        throw new IllegalStateException("인증되지 않은 사용자입니다.");
    }
}
