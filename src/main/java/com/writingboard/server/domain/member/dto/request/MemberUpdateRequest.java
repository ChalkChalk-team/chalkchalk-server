package com.writingboard.server.domain.member.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberUpdateRequest {

    @Size(max = 50, message = "닉네임은 2자 이상 50자 이하이어야 합니다.")
    private String name;

    @Size(max = 500, message = "URL 길이가 너무 깁니다.")
    private String profileImageUrl;
}