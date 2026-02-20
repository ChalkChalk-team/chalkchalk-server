package com.writingboard.server.domain.team.dto.response;

import com.writingboard.server.domain.drawing.document.DrawingSnapshot;
import com.writingboard.server.domain.team.entity.TeamAsset;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class TeamAssetNoteDataResponse {

    private Long teamAssetId;
    private String name;
    private String pdfDownloadUrl;
    private Integer totalPages;
    private List<DrawingPageData> drawingData;

    public static TeamAssetNoteDataResponse of(TeamAsset asset, String pdfDownloadUrl, List<DrawingPageData> drawingData) {
        return TeamAssetNoteDataResponse.builder()
                .teamAssetId(asset.getId())
                .name(asset.getName())
                .pdfDownloadUrl(pdfDownloadUrl)
                .totalPages(asset.getTotalPages())
                .drawingData(drawingData)
                .build();
    }

    @Getter
    @Builder
    public static class DrawingPageData {
        private Integer pageIndex;
        private Long version;
        private String snapshotData;
        private Instant createdAt;

        public static DrawingPageData from(DrawingSnapshot snapshot) {
            return DrawingPageData.builder()
                    .pageIndex(snapshot.getPageIndex())
                    .version(snapshot.getVersion())
                    .snapshotData(snapshot.getSnapshotData())
                    .createdAt(snapshot.getCreatedAt())
                    .build();
        }
    }
}
