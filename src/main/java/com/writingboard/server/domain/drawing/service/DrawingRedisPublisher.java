package com.writingboard.server.domain.drawing.service;

import com.writingboard.server.domain.drawing.dto.response.DrawingStrokeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class DrawingRedisPublisher {

    private static final String CHANNEL_PREFIX = "drawing:room:";

    private final RedisTemplate<String, DrawingStrokeDto> drawingRedisTemplate;

    /**
     * 드로잉 스트로크 메시지를 Redis 채널에 발행
     */
    public void publish(String roomUuid, DrawingStrokeDto strokeDto) {
        String channel = CHANNEL_PREFIX + roomUuid;

        try {
            drawingRedisTemplate.convertAndSend(channel, strokeDto);
            log.debug("Redis 드로잉 메시지 발행: channel={}, type={}, pageIndex={}",
                    channel, strokeDto.getType(), strokeDto.getPageIndex());
        } catch (Exception e) {
            log.error("Redis 드로잉 메시지 발행 실패: channel={}", channel, e);
        }
    }
}
