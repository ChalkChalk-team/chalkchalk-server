package com.writingboard.server.domain.meeting.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NonNull;

@Data
public class InviteLinkRequest {

    @Min(value = 1, message = "최소 1분 이상")
    @Max(value = 60, message = "최대 60분까지")
    private Integer expiresInMinutes = 5;

    @NonNull
    @Size(max = 50, message = "이름은 최대 50자까지 가능합니다.")
    private String name;
}
