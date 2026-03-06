package com.writingboard.server.domain.drawing.event;

import com.writingboard.server.domain.drawing.dto.response.DrawingStrokeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class DrawingEventListener {

    private static final String BUFFER_KEY_PREFIX = "drawing:buffer:";

    private final RedisTemplate<String, DrawingStrokeDto> drawingRedisTemplate;

    @Async("storageTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRedisBufferTrimEvent(RedisBufferTrimEvent event) {
        String bufferKey = BUFFER_KEY_PREFIX + "room:" + event.roomUuid()
                + ":asset:" + event.roomAssetId()
                + ":page:" + event.pageIndex();
        try {
            drawingRedisTemplate.opsForList().trim(bufferKey, event.version(), -1);
            log.info("Redis 버퍼 정리 완료: bufferKey={}, trimmedUpTo={}", bufferKey, event.version());
        } catch (Exception e) {
            log.error("Redis 버퍼 정리 실패: bufferKey={}, version={}", bufferKey, event.version(), e);
        }
    }
}
