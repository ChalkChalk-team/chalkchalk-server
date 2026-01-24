package com.writingboard.server.global.auth.service;

import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 구글에서 유저 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);
        log.info("구글 로그인 정보: {}", oAuth2User.getAttributes());

        // 2. 정보 추출
        String provider = userRequest.getClientRegistration().getRegistrationId(); // "google"
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String providerId = (String) attributes.get("sub"); // 구글의 PK

        // 3. DB 저장 (있으면 패스, 없으면 저장)
        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> {
                    log.info("신규 회원가입: {}", email);
                    return memberRepository.save(Member.builder()
                            .email(email)
                            .name(name)
                            .providerId(providerId)
                            .build());
                });

        return oAuth2User; // 일단 리턴 (나중에 여기서 CustomUserDetail 리턴해야 함)
    }
}