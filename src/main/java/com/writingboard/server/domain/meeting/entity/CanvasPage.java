package com.writingboard.server.domain.meeting.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "canvas_page", uniqueConstraints = {
        @UniqueConstraint(name = "uk_canvas_page_asset_index", columnNames = {"room_asset_id", "page_index"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CanvasPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "canvas_page_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_asset_id", nullable = false, foreignKey = @ForeignKey(name = "fk_canvas_page_room_asset"))
    private RoomAsset roomAsset;

    @Column(name = "page_index", nullable = false)
    private Integer pageIndex;

    @Lob
    @Column(name = "raw_data", columnDefinition = "LONGBLOB")
    private byte[] rawData;

    @Column(name = "last_updated_at", nullable = false)
    private Instant lastUpdatedAt;

    public static CanvasPage create(RoomAsset roomAsset, int pageIndex) {
        CanvasPage page = new CanvasPage();
        page.roomAsset = roomAsset;
        page.pageIndex = pageIndex;
        page.rawData = null;
        page.lastUpdatedAt = Instant.now();
        return page;
    }

    public void updateData(byte[] data) {
        this.rawData = data;
        this.lastUpdatedAt = Instant.now();
    }
}
