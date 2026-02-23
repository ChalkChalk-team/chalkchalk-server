package com.writingboard.server.domain.auth.dto.internal;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SocialUserInfo {

    private final String socialId;  // provider의 sub (고유 식별자)
    private final String email;     // nullable - Apple은 최초 로그인 이후 미제공
    private final String name;      // nullable
}
