package com.writingboard.server.domain.member.controller;

import com.writingboard.server.domain.member.dto.request.DeactivateDeviceTokenRequest;
import com.writingboard.server.domain.member.dto.request.RegisterDeviceTokenRequest;
import com.writingboard.server.domain.member.service.MemberDeviceTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members/me/device-tokens")
@RequiredArgsConstructor
public class MemberDeviceTokenController {

    private final MemberDeviceTokenService memberDeviceTokenService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid RegisterDeviceTokenRequest request
    ) {
        memberDeviceTokenService.registerOrUpdate(memberId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deactivate")
    public ResponseEntity<Void> deactivate(
            @AuthenticationPrincipal Long memberId,
            @RequestBody @Valid DeactivateDeviceTokenRequest request
    ) {
        memberDeviceTokenService.deactivate(memberId, request);
        return ResponseEntity.ok().build();
    }
}
