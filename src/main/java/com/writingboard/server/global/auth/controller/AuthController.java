package com.writingboard.server.global.auth.controller;

import com.writingboard.server.global.auth.dto.request.CodeExchangeRequest;
import com.writingboard.server.global.auth.dto.response.TokenResponse;
import com.writingboard.server.global.auth.service.TempCodeService;
import com.writingboard.server.global.auth.service.TempCodeService.TokenSet;
import com.writingboard.server.global.common.dto.ErrorResponse;
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
public class AuthController {

    private final TempCodeService tempCodeService;

    /**
     * 임시 코드를 실제 토큰으로 교환 (iOS 앱 전용)
     */
    @PostMapping("/exchange")
    public ResponseEntity<?> exchangeToken(@RequestBody @Valid CodeExchangeRequest request) {

        TokenSet tokens = tempCodeService.exchangeCode(request.getCode());

        if (tokens == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.of(
                            "INVALID_CODE",
                            "유효하지 않거나 이미 사용된 코드입니다."
                    ));
        }


        return ResponseEntity.ok(TokenResponse.builder()
                .accessToken(tokens.getAccessToken())
                .refreshToken(tokens.getRefreshToken())
                .tokenType("Bearer")
                .expiresIn(1800) // 30분
                .build());
    }
}