package com.writingboard.server.domain.team.dto.response;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
public class AssetListResponse {

    private List<AssetResponse> assets;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;
    private boolean hasPrevious;

    public static AssetListResponse of(Page<AssetResponse> assetPage) {
        return AssetListResponse.builder()
                .assets(assetPage.getContent())
                .page(assetPage.getNumber())
                .size(assetPage.getSize())
                .totalElements(assetPage.getTotalElements())
                .totalPages(assetPage.getTotalPages())
                .hasNext(assetPage.hasNext())
                .hasPrevious(assetPage.hasPrevious())
                .build();
    }
}
