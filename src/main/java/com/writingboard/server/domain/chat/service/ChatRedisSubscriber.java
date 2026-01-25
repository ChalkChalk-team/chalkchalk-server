package com.writingboard.server.domain.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.writingboard.server.domain.chat.dto.response.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRedisSubscriber implements MessageListener {

    private static final String CHANNEL_PREFIX = "chat:room:";
    private static final String TOPIC_PREFIX = "/topic/room/";

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String body = new String(message.getBody());

            ChatMessageDto chatMessage = objectMapper.readValue(body, ChatMessageDto.class);
            String roomUuid = extractRoomUuid(channel);

            // 해당 서버 인스턴스의 로컬 구독자에게 전송
            String destination = TOPIC_PREFIX + roomUuid;
            messagingTemplate.convertAndSend(destination, chatMessage);

            log.debug("로컬 구독자에게 메시지 전송: destination={}, messageId={}",
                    destination, chatMessage.getId());

        } catch (Exception e) {
            log.error("Redis 메시지 처리 실패", e);
        }
    }

    private String extractRoomUuid(String channel) {
        // chat:room:{roomUuid} -> roomUuid 추출
        return channel.replace(CHANNEL_PREFIX, "");
    }
}
