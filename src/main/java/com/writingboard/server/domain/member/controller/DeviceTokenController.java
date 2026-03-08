package com.writingboard.server.domain.member.controller;

import com.writingboard.server.domain.member.dto.request.DeviceTokenRegisterRequest;
import com.writingboard.server.domain.member.dto.response.DeviceTokenResponse;
import com.writingboard.server.domain.member.service.DeviceTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/members/me/device-tokens")
@RequiredArgsConstructor
public class DeviceTokenController {

    private final DeviceTokenService deviceTokenService;

    /**
     * 디바이스 토큰 등록
     */
    @PostMapping
    public ResponseEntity<DeviceTokenResponse> registerDeviceToken(
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid DeviceTokenRegisterRequest request) {
        return ResponseEntity.ok(deviceTokenService.registerDeviceToken(memberId, request));
    }
}
