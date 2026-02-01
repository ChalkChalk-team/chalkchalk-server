package com.writingboard.server.domain.meeting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowStateDto {

    private Long memberId;
    private String memberName;
    private Long roomAssetId;
    private Integer pageIndex;
    private Instant timestamp;

    public static FollowStateDto of(Long memberId, String memberName, Long roomAssetId, Integer pageIndex) {
        return FollowStateDto.builder()
                .memberId(memberId)
                .memberName(memberName)
                .roomAssetId(roomAssetId)
                .pageIndex(pageIndex)
                .timestamp(Instant.now())
                .build();
    }
}
