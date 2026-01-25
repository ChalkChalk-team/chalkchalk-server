package com.writingboard.server.domain.member.service;

import com.writingboard.server.domain.member.dto.request.MemberUpdateRequest;
import com.writingboard.server.domain.member.dto.response.MemberProfileResponse;
import com.writingboard.server.domain.member.entity.Member;
import com.writingboard.server.domain.member.exception.ErrorCode;
import com.writingboard.server.domain.member.exception.MemberException;
import com.writingboard.server.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * 내 정보 조회
     */
    public MemberProfileResponse getMyProfile(Long memberId) {
        Member member = getMemberById(memberId);
        return MemberProfileResponse.of(member);
    }

    /**
     * 내 정보 수정 (이름, 프로필 사진)
     */
    @Transactional
    public MemberProfileResponse updateProfile(Long memberId, MemberUpdateRequest request) {
        Member member = getMemberById(memberId);

        // 엔티티 비즈니스 메서드 호출 (Dirty Checking)
        member.updateProfile(request.getName(), request.getProfileImageUrl());

        return MemberProfileResponse.of(member);
    }


    /**
     * 회원 탈퇴 (Soft Delete)
     */
    @Transactional
    public void withdraw(Long memberId) {
        Member member = getMemberById(memberId);

        // 이미 탈퇴했는지 재확인
        if (member.isDeleted()) {
            throw new MemberException(ErrorCode.ALREADY_DELETED);
        }

        member.withdraw();
    }


    private Member getMemberById(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.isDeleted()) {
            throw new MemberException(ErrorCode.ALREADY_DELETED);
        }
        return member;
    }
}