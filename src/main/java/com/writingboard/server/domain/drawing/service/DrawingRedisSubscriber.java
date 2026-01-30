package com.writingboard.server.domain.drawing.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.writingboard.server.domain.drawing.dto.response.DrawingStrokeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * 드로잉 Redis 메시지 구독 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DrawingRedisSubscriber implements MessageListener {

    private static final String CHANNEL_PREFIX = "drawing:room:";
    private static final String TOPIC_PREFIX = "/topic/room/";
    private static final String DRAWING_SUFFIX = "/drawing";

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Redis에서 메시지를 수신하여 WebSocket으로 브로드캐스트
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String body = new String(message.getBody());

            DrawingStrokeDto strokeDto = objectMapper.readValue(body, DrawingStrokeDto.class);
            String roomUuid = extractRoomUuid(channel);

            String destination = TOPIC_PREFIX + roomUuid + DRAWING_SUFFIX;
            messagingTemplate.convertAndSend(destination, strokeDto);

            log.debug("클라이언트로 드로잉 메시지 전송: destination={}, type={}, pageIndex={}",
                    destination, strokeDto.getType(), strokeDto.getPageIndex());

        } catch (Exception e) {
            log.error("Redis 드로잉 메시지 처리 실패", e);
        }
    }

    /**
     * Redis 채널명에서 roomUuid 추출
     */
    private String extractRoomUuid(String channel) {
        return channel.replace(CHANNEL_PREFIX, "");
    }
}
