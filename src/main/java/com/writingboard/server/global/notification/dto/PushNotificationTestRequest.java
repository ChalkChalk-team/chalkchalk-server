package com.writingboard.server.global.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PushNotificationTestRequest(
    @NotBlank(message = "디바이스 토큰은 필수입니다")
    @Pattern(regexp = "^[0-9a-fA-F]{64}$", message = "유효하지 않은 APNs 토큰 형식입니다")
    String deviceToken,

    @NotBlank(message = "알림 제목을 입력해주세요")
    @Size(max = 100, message = "알림 제목은 100자 이내여야 합니다")
    String title,

    @NotBlank(message = "알림 내용을 입력해주세요")
    @Size(max = 500, message = "알림 내용은 500자 이내여야 합니다")
    String body
) {
}
