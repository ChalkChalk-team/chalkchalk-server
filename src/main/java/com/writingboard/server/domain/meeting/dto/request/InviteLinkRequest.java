package com.writingboard.server.domain.meeting.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class InviteLinkRequest {

    @Min(value = 1, message = "최소 1시간 이상이어야 합니다")
    @Max(value = 168, message = "최대 7일(168시간)까지 설정 가능합니다")
    private Integer expiresInHours;
}
