package com.writingboard.server.domain.meeting.service;

import com.writingboard.server.domain.meeting.dto.FollowStateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Deprecated
@Slf4j
@Service
@RequiredArgsConstructor
public class CanvasFollowRedisPublisher {

    private static final String CHANNEL_PREFIX = "follow:room:";

    private final RedisTemplate<String, FollowStateDto> followRedisTemplate;

    public void publish(String roomUuid, FollowStateDto state) {
        String channel = CHANNEL_PREFIX + roomUuid;

        try {
            followRedisTemplate.convertAndSend(channel, state);
            log.debug("Follow state published: channel={}, memberId={}", channel, state.getMemberId());
        } catch (Exception e) {
            log.error("Failed to publish follow state: channel={}", channel, e);
        }
    }
}
