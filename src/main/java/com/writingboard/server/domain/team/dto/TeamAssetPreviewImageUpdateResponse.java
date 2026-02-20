package com.writingboard.server.domain.team.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamAssetPreviewImageUpdateResponse {

    private Long assetId;
    private String previewImageUrl;

    public static TeamAssetPreviewImageUpdateResponse of(Long assetId, String previewImageUrl) {
        return TeamAssetPreviewImageUpdateResponse.builder()
                .assetId(assetId)
                .previewImageUrl(previewImageUrl)
                .build();
    }
}
