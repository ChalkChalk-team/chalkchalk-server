package com.writingboard.server.domain.meeting.dto.response;

import com.writingboard.server.domain.meeting.entity.enums.ParticipantRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ParticipantViewingResponse {

    private Long memberId;
    private String memberName;
    private ParticipantRole role;
    private Long roomAssetId;
    private String assetName;
    private Integer pageIndex;
}
