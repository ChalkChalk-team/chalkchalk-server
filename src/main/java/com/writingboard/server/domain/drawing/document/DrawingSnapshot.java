package com.writingboard.server.domain.drawing.document;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * 드로잉 스냅샷 MongoDB 문서
 * 페이지별 PKDrawing 병합 데이터 저장
 */
@Document(collection = "drawing_snapshots")
@CompoundIndex(name = "idx_room_page_version", def = "{'roomUuid': 1, 'pageIndex': 1, 'version': -1}")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DrawingSnapshot {

    @Id
    private String id;

    @Indexed
    private String roomUuid;

    @Indexed
    private Integer pageIndex;

    private Long version;

    private String snapshotData;

    private Long createdBy;
    private String createdByName;

    @Indexed
    private Instant createdAt;

    private DrawingSnapshot(String roomUuid, Integer pageIndex, Long version,
                           String snapshotData, Long createdBy, String createdByName) {
        this.roomUuid = roomUuid;
        this.pageIndex = pageIndex;
        this.version = version;
        this.snapshotData = snapshotData;
        this.createdBy = createdBy;
        this.createdByName = createdByName;
        this.createdAt = Instant.now();
    }

    /**
     * 드로잉 스냅샷 생성 팩토리 메서드
     */
    public static DrawingSnapshot create(String roomUuid, Integer pageIndex, Long version,
                                        String snapshotData, Long createdBy, String createdByName) {
        return new DrawingSnapshot(roomUuid, pageIndex, version, snapshotData, createdBy, createdByName);
    }
}
