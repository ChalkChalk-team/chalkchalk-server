package com.writingboard.server.domain.meeting.dto.response;

import com.writingboard.server.domain.meeting.entity.CanvasSnapshot;
import com.writingboard.server.domain.meeting.entity.enums.SnapshotTriggerType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Deprecated
@Getter
@Builder
@AllArgsConstructor
public class CanvasSnapshotResponse {

    private Long id;
    private Long roomAssetId;
    private Integer pageIndex;
    private SnapshotTriggerType triggerType;
    private Instant createdAt;

    public static CanvasSnapshotResponse from(CanvasSnapshot snapshot) {
        return CanvasSnapshotResponse.builder()
                .id(snapshot.getId())
                .roomAssetId(snapshot.getRoomAsset().getId())
                .pageIndex(snapshot.getPageIndex())
                .triggerType(snapshot.getTriggerType())
                .createdAt(snapshot.getCreatedAt())
                .build();
    }
}
