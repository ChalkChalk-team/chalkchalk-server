package com.writingboard.server.domain.member.dto.response;

import com.writingboard.server.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberProfileResponse {

    private Long memberId;
    private String email;
    private String name;
    private String userId;
    private String nickname;
    private String displayName;
    private String profileImageUrl;

    public static MemberProfileResponse of(Member member) {
        return MemberProfileResponse.builder()
                .memberId(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .userId(member.getUserId())
                .nickname(member.getNickname())
                .displayName(member.getDisplayName())
                .profileImageUrl(member.getProfileImageUrl())
                .build();
    }
}