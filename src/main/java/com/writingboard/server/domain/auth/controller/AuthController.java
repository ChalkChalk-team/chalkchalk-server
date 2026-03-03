package com.writingboard.server.domain.auth.controller;

import com.writingboard.server.domain.auth.dto.request.GuestLoginRequest;
import com.writingboard.server.domain.auth.dto.request.RefreshTokenRequest;
import com.writingboard.server.domain.auth.dto.request.SocialLoginRequest;
import com.writingboard.server.domain.auth.dto.response.TokenResponse;
import com.writingboard.server.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 API")
public class AuthController {

    private final AuthService authService;

    /**
     * 게스트 로그인 - deviceId로 회원 조회 또는 생성 후 JWT 발급
     */
    @Operation(summary = "게스트 로그인", description = "deviceId로 기존 회원 조회 또는 신규 생성 후 JWT 토큰 발급")
    @PostMapping("/guest-login")
    public ResponseEntity<TokenResponse> guestLogin(@RequestBody @Valid GuestLoginRequest request) {
        TokenResponse response = authService.guestLogin(request.getDeviceId(), request.getName());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * 소셜 로그인 - idToken 검증 후 JWT 발급
     */
    @Operation(summary = "소셜 로그인", description = "iOS 앱에서 획득한 idToken을 검증하여 JWT 토큰 발급 (provider: GOOGLE, APPLE)")
    @PostMapping("/social-login")
    public ResponseEntity<TokenResponse> socialLogin(@RequestBody @Valid SocialLoginRequest request) {
        TokenResponse response = authService.socialLogin(request.getProvider(), request.getIdToken(), request.getUserId(), request.getNickname());
        return ResponseEntity.ok(response);
    }

    /**
     * Refresh Token으로 새로운 Access Token 발급
     */
    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새로운 Access Token 발급")
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody @Valid RefreshTokenRequest request) {
        TokenResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
}