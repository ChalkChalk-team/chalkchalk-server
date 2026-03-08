package com.writingboard.server.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DeviceTokenRegisterRequest {

    @NotBlank(message = "디바이스 토큰은 필수입니다")
    @Pattern(regexp = "^[0-9a-fA-F]{64}$", message = "유효하지 않은 APNs 디바이스 토큰 형식입니다")
    private String token;

    @Size(max = 50, message = "디바이스 모델은 50자 이내여야 합니다")
    private String deviceModel;

    @Size(max = 20, message = "OS 버전은 20자 이내여야 합니다")
    private String osVersion;
}
