package com.writingboard.server.domain.meeting.entity;

import com.writingboard.server.domain.meeting.entity.enums.SnapshotTriggerType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "canvas_snapshot", indexes = {
        @Index(name = "idx_canvas_snapshot_asset_page", columnList = "room_asset_id, page_index")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CanvasSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "canvas_snapshot_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_asset_id", nullable = false, foreignKey = @ForeignKey(name = "fk_canvas_snapshot_room_asset"))
    private RoomAsset roomAsset;

    @Column(name = "page_index", nullable = false)
    private Integer pageIndex;

    @Lob
    @Column(name = "stroke_data", columnDefinition = "LONGBLOB")
    private byte[] strokeData;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", length = 20, nullable = false)
    private SnapshotTriggerType triggerType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public static CanvasSnapshot create(RoomAsset roomAsset, int pageIndex, byte[] strokeData, SnapshotTriggerType triggerType) {
        CanvasSnapshot snapshot = new CanvasSnapshot();
        snapshot.roomAsset = roomAsset;
        snapshot.pageIndex = pageIndex;
        snapshot.strokeData = strokeData;
        snapshot.triggerType = triggerType;
        snapshot.createdAt = Instant.now();
        return snapshot;
    }
}
