package com.writingboard.server.domain.chat.service;

import com.writingboard.server.domain.chat.dto.response.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRedisPublisher {

    private static final String CHANNEL_PREFIX = "chat:room:";

    private final RedisTemplate<String, ChatMessageDto> chatRedisTemplate;

    public void publish(String roomUuid, ChatMessageDto message) {
        String channel = CHANNEL_PREFIX + roomUuid;

        try {
            chatRedisTemplate.convertAndSend(channel, message);
            log.debug("Redis 메시지 발행: channel={}, messageId={}", channel, message.getId());
        } catch (Exception e) {
            log.error("Redis 메시지 발행 실패: channel={}", channel, e);
            // MongoDB에 이미 저장되어 있으므로 실패해도 무시
        }
    }
}
