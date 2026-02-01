package com.writingboard.server.domain.meeting.dto.request;

import com.writingboard.server.domain.meeting.entity.enums.SavePolicy;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoadTeamAssetRequest {

    @NotNull(message = "팀 자료 ID는 필수입니다")
    private Long teamAssetId;

    private SavePolicy savePolicy;
}
