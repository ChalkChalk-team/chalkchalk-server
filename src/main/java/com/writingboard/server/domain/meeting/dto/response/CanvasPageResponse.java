package com.writingboard.server.domain.meeting.dto.response;

import com.writingboard.server.domain.meeting.entity.CanvasPage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.Base64;

@Deprecated
@Getter
@Builder
@AllArgsConstructor
public class CanvasPageResponse {

    private Long id;
    private Long roomAssetId;
    private Integer pageIndex;
    private String rawData;
    private Instant lastUpdatedAt;

    public static CanvasPageResponse from(CanvasPage canvasPage) {
        String encodedData = null;
        if (canvasPage.getRawData() != null) {
            encodedData = Base64.getEncoder().encodeToString(canvasPage.getRawData());
        }

        return CanvasPageResponse.builder()
                .id(canvasPage.getId())
                .roomAssetId(canvasPage.getRoomAsset().getId())
                .pageIndex(canvasPage.getPageIndex())
                .rawData(encodedData)
                .lastUpdatedAt(canvasPage.getLastUpdatedAt())
                .build();
    }

    public static CanvasPageResponse empty(Long roomAssetId, int pageIndex) {
        return CanvasPageResponse.builder()
                .id(null)
                .roomAssetId(roomAssetId)
                .pageIndex(pageIndex)
                .rawData(null)
                .lastUpdatedAt(null)
                .build();
    }
}
