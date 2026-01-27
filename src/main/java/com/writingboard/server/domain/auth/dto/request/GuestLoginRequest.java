package com.writingboard.server.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GuestLoginRequest {

    @NotBlank(message = "deviceId는 필수입니다")
    @Size(min = 10, max = 100, message = "deviceId는 10자 이상 100자 이하여야 합니다")
    private String deviceId;

    @Size(max = 50, message = "이름은 50자 이하여야 합니다")
    private String name;
}
