package com.writingboard.server.domain.team.dto.response;

import com.writingboard.server.domain.team.entity.TeamAsset;
import com.writingboard.server.domain.team.entity.enums.AssetSourceType;
import com.writingboard.server.domain.team.entity.enums.AssetType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AssetResponse {

    private Long assetId;
    private Long teamId;
    private AssetType type;
    private String name;
    private AssetSourceType sourceType;
    private Long originPersonalAssetId;
    private Integer version;
    private Boolean isLatest;
    private Long parentAssetId;
    private String storageKey;
    private Integer totalPages;
    private String thumbnailImageUrl;
    private UploaderInfo uploader;
    private Instant createdAt;
    private Instant updatedAt;

    public static AssetResponse of(TeamAsset asset) {
        return AssetResponse.builder()
                .assetId(asset.getId())
                .teamId(asset.getTeam().getId())
                .type(asset.getType())
                .name(asset.getName())
                .sourceType(asset.getSourceType())
                .originPersonalAssetId(asset.getOriginPersonalAssetId())
                .version(asset.getVersion())
                .isLatest(asset.getIsLatest())
                .parentAssetId(asset.getParentAsset() != null ? asset.getParentAsset().getId() : null)
                .storageKey(asset.getStorageKey())
                .totalPages(asset.getTotalPages())
                .uploader(UploaderInfo.of(asset))
                .createdAt(asset.getCreatedAt())
                .updatedAt(asset.getUpdatedAt())
                .build();
    }

    @Data
    @Builder
    public static class UploaderInfo {
        private Long memberId;
        private String name;

        public static UploaderInfo of(TeamAsset asset) {
            return UploaderInfo.builder()
                    .memberId(asset.getUploader().getId())
                    .name(asset.getUploader().getName())
                    .build();
        }
    }
}
