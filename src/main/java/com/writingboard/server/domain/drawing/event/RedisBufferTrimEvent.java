package com.writingboard.server.domain.drawing.event;

public record RedisBufferTrimEvent(
        String roomUuid,
        Long roomAssetId,
        Integer pageIndex,
        Long version
) {
}
