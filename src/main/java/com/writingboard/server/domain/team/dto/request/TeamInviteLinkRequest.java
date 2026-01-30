package com.writingboard.server.domain.team.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class TeamInviteLinkRequest {

    @Min(value = 1, message = "최소 1분 이상이어야 합니다")
    @Max(value = 10080, message = "최대 7일(10080분)까지 가능합니다")
    private Integer expiresInMinutes = 60;
}
