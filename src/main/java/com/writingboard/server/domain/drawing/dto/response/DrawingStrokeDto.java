package com.writingboard.server.domain.drawing.dto.response;

import com.writingboard.server.domain.drawing.enums.DrawingMessageType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Getter
@NoArgsConstructor
public class DrawingStrokeDto {

    private String roomUuid;
    private Long senderId;
    private String senderName;
    private DrawingMessageType type;
    private Integer pageIndex;
    private String strokeId;
    private String strokeData;
    private Long version;
    private Instant timestamp;

    @Builder
    public DrawingStrokeDto(String roomUuid, Long senderId, String senderName,
                            DrawingMessageType type, Integer pageIndex,
                            String strokeId, String strokeData, Long version,
                            Instant timestamp) {
        this.roomUuid = roomUuid;
        this.senderId = senderId;
        this.senderName = senderName;
        this.type = type;
        this.pageIndex = pageIndex;
        this.strokeId = strokeId;
        this.strokeData = strokeData;
        this.version = version;
        this.timestamp = timestamp;
    }

    /**
     * DrawingStrokeDto 생성 팩토리 메서드
     */
    public static DrawingStrokeDto of(String roomUuid, Long senderId, String senderName,
                                      DrawingMessageType type, Integer pageIndex,
                                      String strokeId, String strokeData, Long version) {
        return DrawingStrokeDto.builder()
                .roomUuid(roomUuid)
                .senderId(senderId)
                .senderName(senderName)
                .type(type)
                .pageIndex(pageIndex)
                .strokeId(strokeId)
                .strokeData(strokeData)
                .version(version)
                .timestamp(Instant.now())
                .build();
    }
}
