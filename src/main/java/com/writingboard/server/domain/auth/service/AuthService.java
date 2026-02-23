package com.writingboard.server.domain.auth.service;

import com.writingboard.server.domain.auth.dto.internal.SocialUserInfo;
import com.writingboard.server.domain.auth.dto.response.TokenResponse;
import com.writingboard.server.domain.auth.exception.AuthErrorCode;
import com.writingboard.server.domain.auth.exception.AuthException;
import com.writingboard.server.domain.auth.jwt.JwtProvider;
import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.member.entity.enums.AuthProvider;
import com.writingboard.server.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final List<SocialTokenVerifier> socialTokenVerifiers;

    /**
     * 게스트 로그인 - deviceId로 회원 조회 또는 생성
     */
    @Transactional
    public TokenResponse guestLogin(String deviceId, String name) {
        log.info("Guest login attempt - deviceId: {}", deviceId);

        Member member = memberRepository.findByProviderIdAndProvider(deviceId, AuthProvider.GUEST)
                .orElseGet(() -> {
                    log.info("Creating new guest member - deviceId: {}", deviceId);
                    Member newMember = Member.createGuest(deviceId, name);
                    return memberRepository.save(newMember);
                });

        log.info("Guest login successful - memberId: {}, deviceId: {}", member.getId(), deviceId);

        return generateTokenResponse(member);
    }

    /**
     * 소셜 로그인 - idToken 검증 후 회원 조회 또는 생성
     */
    @Transactional
    public TokenResponse socialLogin(AuthProvider provider, String idToken) {
        log.info("Social login attempt - provider: {}", provider);

        SocialTokenVerifier verifier = socialTokenVerifiers.stream()
                .filter(v -> v.getProvider() == provider)
                .findFirst()
                .orElseThrow(() -> new AuthException(AuthErrorCode.UNSUPPORTED_PROVIDER));

        SocialUserInfo userInfo = verifier.verify(idToken);

        Member member = memberRepository.findByProviderIdAndProvider(userInfo.getSocialId(), provider)
                .orElseGet(() -> {
                    log.info("Creating new social member - provider: {}, socialId: {}", provider, userInfo.getSocialId());
                    String email = userInfo.getEmail() != null
                            ? userInfo.getEmail()
                            : provider.name().toLowerCase() + "-" + userInfo.getSocialId() + "@temp.local";
                    String name = userInfo.getName() != null ? userInfo.getName() : provider.name() + "-User";
                    Member newMember = Member.create(email, name, userInfo.getSocialId(), null, provider);
                    return memberRepository.save(newMember);
                });

        log.info("Social login successful - provider: {}, memberId: {}", provider, member.getId());

        return generateTokenResponse(member);
    }

    /**
     * Refresh Token으로 새로운 Access Token 발급
     */
    public TokenResponse refreshToken(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new AuthException(AuthErrorCode.INVALID_TOKEN);
        }

        Long memberId = jwtProvider.getMemberIdFromToken(refreshToken);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.UNAUTHORIZED));

        log.info("Token refresh successful - memberId: {}", memberId);

        return generateTokenResponse(member);
    }

    /**
     * JWT 토큰 생성 및 응답 DTO 반환
     */
    private TokenResponse generateTokenResponse(Member member) {
        String accessToken = jwtProvider.createAccessToken(member.getId(), "USER");
        String refreshToken = jwtProvider.createRefreshToken(member.getId());

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(1800)
                .build();
    }
}
