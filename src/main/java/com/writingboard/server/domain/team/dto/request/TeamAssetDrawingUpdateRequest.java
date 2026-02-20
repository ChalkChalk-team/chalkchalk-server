package com.writingboard.server.domain.team.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TeamAssetDrawingUpdateRequest {

    @NotNull(message = "pageIndex는 필수입니다")
    @Min(value = 0, message = "pageIndex는 0 이상이어야 합니다")
    private Integer pageIndex;

    @NotBlank(message = "snapshotData는 필수입니다")
    private String snapshotData;
}
