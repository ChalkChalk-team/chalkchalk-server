package com.writingboard.server.domain.meeting.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class InviteLinkRequest {

    @Min(value = 1, message = "최소 1분 이상")
    @Max(value = 60, message = "최대 60분까지")
    private Integer expiresInMinutes = 5;

    @NotBlank(message = "사용자 ID는 필수입니다.")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$", message = "사용자 ID는 영문, 숫자, 밑줄만 사용 가능하며 3~20자여야 합니다.")
    private String userId;
}
