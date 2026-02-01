package com.writingboard.server.domain.meeting.dto.response;

import com.writingboard.server.domain.meeting.entity.RoomAsset;
import com.writingboard.server.domain.meeting.entity.enums.SavePolicy;
import com.writingboard.server.domain.team.entity.enums.AssetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@AllArgsConstructor
public class RoomAssetResponse {

    private Long id;
    private Long teamAssetId;
    private String assetName;
    private AssetType assetType;
    private Integer totalPages;
    private Integer currentPageNumber;
    private Boolean isActive;
    private SavePolicy savePolicy;
    private Long addedById;
    private String addedByName;
    private Instant createdAt;

    public static RoomAssetResponse from(RoomAsset roomAsset) {
        RoomAssetResponseBuilder builder = RoomAssetResponse.builder()
                .id(roomAsset.getId())
                .currentPageNumber(roomAsset.getCurrentPageNumber())
                .isActive(roomAsset.getIsActive())
                .savePolicy(roomAsset.getSavePolicy())
                .addedById(roomAsset.getAddedBy().getId())
                .addedByName(roomAsset.getAddedBy().getName())
                .createdAt(roomAsset.getCreatedAt());

        if (roomAsset.getTeamAsset() != null) {
            builder.teamAssetId(roomAsset.getTeamAsset().getId())
                    .assetName(roomAsset.getTeamAsset().getName())
                    .assetType(roomAsset.getTeamAsset().getType())
                    .totalPages(roomAsset.getTeamAsset().getTotalPages());
        }

        return builder.build();
    }
}
