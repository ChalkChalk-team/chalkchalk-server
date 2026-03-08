package com.writingboard.server.domain.member.dto.response;

import com.writingboard.server.domain.member.entity.DeviceToken;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class DeviceTokenResponse {

    private Long deviceTokenId;
    private String token;
    private String deviceModel;
    private String osVersion;
    private Instant createdAt;
    private Instant updatedAt;

    public static DeviceTokenResponse of(DeviceToken deviceToken) {
        return DeviceTokenResponse.builder()
                .deviceTokenId(deviceToken.getId())
                .token(deviceToken.getToken())
                .deviceModel(deviceToken.getDeviceModel())
                .osVersion(deviceToken.getOsVersion())
                .createdAt(deviceToken.getCreatedAt())
                .updatedAt(deviceToken.getUpdatedAt())
                .build();
    }
}
