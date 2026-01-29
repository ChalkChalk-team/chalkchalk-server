package com.writingboard.server.domain.voice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.writingboard.server.domain.voice.dto.response.VoiceSignalDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceRedisSubscriber implements MessageListener {

    private static final String CHANNEL_PREFIX = "voice:room:";
    private static final String TOPIC_PREFIX = "/topic/room/";
    private static final String VOICE_SUFFIX = "/voice";

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String body = new String(message.getBody());

            VoiceSignalDto signal = objectMapper.readValue(body, VoiceSignalDto.class);
            String roomUuid = extractRoomUuid(channel);

            String destination = TOPIC_PREFIX + roomUuid + VOICE_SUFFIX;
            messagingTemplate.convertAndSend(destination, signal);

            log.debug("클라이언트로 시그널 전송: destination={}, signalType={}",
                    destination, signal.getSignalType());

        } catch (Exception e) {
            log.error("Redis 시그널 처리 실패", e);
        }
    }

    private String extractRoomUuid(String channel) {
        return channel.replace(CHANNEL_PREFIX, "");
    }
}
