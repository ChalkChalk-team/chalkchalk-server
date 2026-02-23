package com.writingboard.server.domain.auth.service;

import com.writingboard.server.domain.auth.dto.internal.SocialUserInfo;
import com.writingboard.server.domain.member.entity.enums.AuthProvider;

public interface SocialTokenVerifier {

    /**
     * idToken을 검증하고 소셜 유저 정보를 반환한다.
     *
     * @param idToken iOS 앱에서 전달받은 ID 토큰
     * @return 검증된 소셜 유저 정보 (socialId, email, name)
     */
    SocialUserInfo verify(String idToken);

    /**
     * 이 구현체가 담당하는 AuthProvider를 반환한다.
     */
    AuthProvider getProvider();
}
