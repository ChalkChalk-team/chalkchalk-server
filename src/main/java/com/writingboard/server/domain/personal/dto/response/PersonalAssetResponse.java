package com.writingboard.server.domain.personal.dto.response;

import com.writingboard.server.domain.personal.entity.PersonalAsset;
import com.writingboard.server.domain.team.entity.enums.AssetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class PersonalAssetResponse {

    private Long id;
    private String name;
    private AssetType type;
    private String storageKey;
    private Long sizeBytes;
    private Integer totalPages;
    private Instant createdAt;
    private Instant updatedAt;

    public static PersonalAssetResponse from(PersonalAsset asset) {
        return PersonalAssetResponse.builder()
                .id(asset.getId())
                .name(asset.getName())
                .type(asset.getType())
                .storageKey(asset.getStorageKey())
                .sizeBytes(asset.getSizeBytes())
                .totalPages(asset.getTotalPages())
                .createdAt(asset.getCreatedAt())
                .updatedAt(asset.getUpdatedAt())
                .build();
    }
}
