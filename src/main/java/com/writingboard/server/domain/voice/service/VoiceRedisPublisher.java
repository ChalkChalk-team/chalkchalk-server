package com.writingboard.server.domain.voice.service;

import com.writingboard.server.domain.voice.dto.response.VoiceSignalDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceRedisPublisher {

    private static final String CHANNEL_PREFIX = "voice:room:";

    private final RedisTemplate<String, VoiceSignalDto> voiceRedisTemplate;

    public void publish(String roomUuid, VoiceSignalDto signal) {
        String channel = CHANNEL_PREFIX + roomUuid;

        try {
            voiceRedisTemplate.convertAndSend(channel, signal);
            log.debug("Redis 시그널 발행: channel={}, signalType={}", channel, signal.getSignalType());
        } catch (Exception e) {
            log.error("Redis 시그널 발행 실패: channel={}", channel, e);
        }
    }
}
