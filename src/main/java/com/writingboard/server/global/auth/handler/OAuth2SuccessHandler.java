package com.writingboard.server.global.auth.handler;

import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.member.repository.MemberRepository;
import com.writingboard.server.global.auth.jwt.JwtProvider;
import com.writingboard.server.global.auth.service.TempCodeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;
    private final TempCodeService tempCodeService;

    @Value("${app.oauth.redirect-url:http://localhost:8080/login/success-test}")
    private String redirectUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = (String) oAuth2User.getAttributes().get("email");

        log.info("📧 OAuth2 로그인 시도: email={}", email);

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("❌ 가입되지 않은 사용자: email={}", email);
                    return new IllegalArgumentException("User not found: " + email);
                });

        // JWT 토큰 발급
        String accessToken = jwtProvider.createAccessToken(member.getId(), "ROLE_USER");
        String refreshToken = jwtProvider.createRefreshToken(member.getId());

        log.debug("🔑 JWT 토큰 생성 완료: memberId={}", member.getId());

        // Redis에 임시 코드 생성 (30초 TTL)
        String tempCode = tempCodeService.createTempCode(accessToken, refreshToken);

        // Deep Link 리다이렉트
        // 프로덕션: livenote://auth/callback?code=...
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
                .queryParam("code", tempCode)
                .build()
                .toUriString();

        log.info("✅ OAuth2 로그인 성공: email={}, memberId={}, code={}",
                email, member.getId(), tempCode);
        log.debug("🔗 리다이렉트 URL: {}", targetUrl);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}