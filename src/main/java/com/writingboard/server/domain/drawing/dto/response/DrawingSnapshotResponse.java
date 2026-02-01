package com.writingboard.server.domain.drawing.dto.response;

import com.writingboard.server.domain.drawing.document.DrawingSnapshot;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;


@Getter
@NoArgsConstructor
public class DrawingSnapshotResponse {

    private String id;
    private String roomUuid;
    private Integer pageIndex;
    private Long version;
    private String snapshotData;
    private Long createdBy;
    private String createdByName;
    private Instant createdAt;

    @Builder
    public DrawingSnapshotResponse(String id, String roomUuid, Integer pageIndex,
                                  Long version, String snapshotData,
                                  Long createdBy, String createdByName, Instant createdAt) {
        this.id = id;
        this.roomUuid = roomUuid;
        this.pageIndex = pageIndex;
        this.version = version;
        this.snapshotData = snapshotData;
        this.createdBy = createdBy;
        this.createdByName = createdByName;
        this.createdAt = createdAt;
    }


    public static DrawingSnapshotResponse from(DrawingSnapshot snapshot) {
        return DrawingSnapshotResponse.builder()
                .id(snapshot.getId())
                .roomUuid(snapshot.getRoomUuid())
                .pageIndex(snapshot.getPageIndex())
                .version(snapshot.getVersion())
                .snapshotData(snapshot.getSnapshotData())
                .createdBy(snapshot.getCreatedBy())
                .createdByName(snapshot.getCreatedByName())
                .createdAt(snapshot.getCreatedAt())
                .build();
    }
}
