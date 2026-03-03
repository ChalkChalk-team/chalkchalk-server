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

    @Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$", message = "사용자 ID는 영문, 숫자, 밑줄만 사용 가능하며 3~20자여야 합니다.")
    private String userId;

    @Size(max = 30, message = "닉네임은 30자 이내여야 합니다.")
    private String nickname;
}