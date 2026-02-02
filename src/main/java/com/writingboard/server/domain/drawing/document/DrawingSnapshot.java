package com.writingboard.server.domain.drawing.document;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;


@Document(collection = "drawing_snapshots")
@CompoundIndex(name = "idx_asset_page_version", def = "{'roomAssetId': 1, 'pageIndex': 1, 'version': -1}")
@CompoundIndex(name = "idx_room_page_version", def = "{'roomUuid': 1, 'pageIndex': 1, 'version': -1}")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DrawingSnapshot {

    @Id
    private String id;

    @Indexed
    private String roomUuid;

    @Indexed
    private Long roomAssetId;

    @Indexed
    private Integer pageIndex;

    private Long version;

    private String snapshotData;

    private Long createdBy;
    private String createdByName;

    @Indexed
    private Instant createdAt;

    private DrawingSnapshot(String roomUuid, Long roomAssetId, Integer pageIndex, Long version,
                           String snapshotData, Long createdBy, String createdByName) {
        this.roomUuid = roomUuid;
        this.roomAssetId = roomAssetId;
        this.pageIndex = pageIndex;
        this.version = version;
        this.snapshotData = snapshotData;
        this.createdBy = createdBy;
        this.createdByName = createdByName;
        this.createdAt = Instant.now();
    }

    public static DrawingSnapshot create(String roomUuid, Long roomAssetId, Integer pageIndex, Long version,
                                        String snapshotData, Long createdBy, String createdByName) {
        return new DrawingSnapshot(roomUuid, roomAssetId, pageIndex, version, snapshotData, createdBy, createdByName);
    }
}
